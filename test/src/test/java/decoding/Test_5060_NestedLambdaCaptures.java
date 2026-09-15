package decoding;

import me.legrange.typelink.QueryPredicate1;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What a lambda nested inside another lambda can see of the enclosing scope.
 *
 * <p>A predicate handed to {@code where(...)} may contain a query of its own - a correlated
 * sub-select over another table, filtered on the row the outer predicate is testing. That works:
 * {@link #correlationWithoutCapture} passes. What does not work is any value the enclosing scope
 * captured.
 *
 * <p>The reason is where each lambda's captured values come from. The outer lambda is the
 * serializable one, so its captures travel with it and are read off the {@code SerializedLambda}.
 * The inner lambda is created at run time inside the outer one's body, and is never serialized, so
 * there is no such record to read. Its captures are the values the enclosing frame pushes onto the
 * stack in front of the {@code INVOKEDYNAMIC} - and {@code BytecodeParser.parseLambda} pops them
 * and throws them away. Nothing downstream can then say what {@code clientNumber} was, and the
 * sub-select's own parse begins with an empty captured-argument list.
 *
 * <p>So an inner lambda may compare a column against a literal, because a literal is in the
 * bytecode, and against a column of the enclosing row, because that resolves by type - but not
 * against anything held in a variable. The four controls at the end of this class draw that line
 * from the other side.
 *
 * <p><b>Why it matters.</b> This is exactly the shape an access control rule takes. A rule resolves
 * against the user once, captures what it learned - the user's client number - and hands back a
 * predicate for the query to apply. The value is only known at run time, so it can never be a
 * literal, and it belongs inside the sub-select, which is where the filtering has to happen. Every
 * rule of that kind is unusable until this works.
 *
 * <p><b>These tests fail as written</b>, except the four controls. They are the specification for
 * the change, not a report of it.
 */
class Test_5060_NestedLambdaCaptures {

    public record Sold(int objKey, String name) {
    }

    public record Sale(int objKey, int soldKey, int clientNumber) {
    }

    public record Client(int clientNumber, String name) {
    }

    // --- the shape an access rule takes ---------------------------------------

    /**
     * A predicate built by one method and applied by another, carrying a value it captured when it
     * was built. Fails: the captured client number never reaches the sub-select.
     */
    @Test
    void predicateBuiltElsewhereCarriesItsCapture() {
        var db = new CapturingDatabase();
        QueryPredicate1<Sold> visible = ownedBy(db, 42);

        db.from(Sold.class).where(visible).list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.soldKey = Sold.objKey AND Sale.clientNumber = ?) > ?", db.sql());
        assertEquals(List.of(42, 0L), db.params());
    }

    private static QueryPredicate1<Sold> ownedBy(CapturingDatabase db, int clientNumber) {
        return s -> db.from(Sale.class)
                .where(x -> x.soldKey() == s.objKey() && x.clientNumber() == clientNumber)
                .count() > 0;
    }

    // --- the same thing written inline ----------------------------------------

    /** Fails. Rules out the helper method as the cause: writing it inline changes nothing. */
    @Test
    void capturedValueInsideNestedLambda() {
        var clientNumber = 42;
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> db.from(Sale.class)
                        .where(x -> x.soldKey() == s.objKey() && x.clientNumber() == clientNumber)
                        .count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.soldKey = Sold.objKey AND Sale.clientNumber = ?) > ?", db.sql());
        assertEquals(List.of(42, 0L), db.params());
    }

    /** Fails. Rules out correlation as the cause: the capture alone is enough to break it. */
    @Test
    void capturedValueInsideNestedLambdaWithoutCorrelation() {
        var clientNumber = 42;
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> db.from(Sale.class)
                        .where(x -> x.clientNumber() == clientNumber)
                        .count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.clientNumber = ?) > ?", db.sql());
        assertEquals(List.of(42, 0L), db.params());
    }

    /** Fails. The sub-select may join, and the capture is no more available there. */
    @Test
    void capturedValueInsideNestedLambdaThatJoins() {
        var clientNumber = 42;
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> db.from(Sale.class)
                        .join(Client.class, (x, c) -> x.clientNumber() == c.clientNumber())
                        .where((x, c) -> x.soldKey() == s.objKey() && c.clientNumber() == clientNumber)
                        .count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " INNER JOIN Client ON Sale.clientNumber = Client.clientNumber"
                + " WHERE Sale.soldKey = Sold.objKey AND Client.clientNumber = ?) > ?", db.sql());
        assertEquals(List.of(42, 0L), db.params());
    }

    // --- the same defect reached through a field ------------------------------

    private int mutableField = 42;
    private final int assignedField;

    Test_5060_NestedLambdaCaptures() {
        this.assignedField = 42;
    }

    /**
     * Fails. Reading a field means capturing {@code this}, so the error names {@code this} rather
     * than the field - the same defect from a different direction.
     */
    @Test
    void instanceFieldInsideNestedLambda() {
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> db.from(Sale.class)
                        .where(x -> x.soldKey() == s.objKey() && x.clientNumber() == this.mutableField)
                        .count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.soldKey = Sold.objKey AND Sale.clientNumber = ?) > ?", db.sql());
        assertEquals(List.of(42, 0L), db.params());
    }

    /**
     * Fails for the same reason. Separate from {@link #instanceFieldInsideNestedLambda} because a
     * {@code final} field with a constant initialiser would be inlined by the compiler and prove
     * nothing; this one is assigned in the constructor, so it is really read.
     */
    @Test
    void constructorAssignedFieldInsideNestedLambda() {
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> db.from(Sale.class)
                        .where(x -> x.soldKey() == s.objKey() && x.clientNumber() == this.assignedField)
                        .count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.soldKey = Sold.objKey AND Sale.clientNumber = ?) > ?", db.sql());
        assertEquals(List.of(42, 0L), db.params());
    }

    /**
     * Two levels deep. The innermost lambda's captures are values of the middle one, which are
     * themselves values of the outermost - so the substitution has to compose, and the captured
     * client number has to survive both steps.
     */
    @Test
    void capturedValueTwoLevelsDown() {
        var clientNumber = 42;
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> db.from(Sale.class)
                        .where(x -> x.soldKey() == s.objKey()
                                && db.from(Client.class)
                                        .where(c -> c.clientNumber() == x.clientNumber()
                                                && c.clientNumber() == clientNumber)
                                        .count() > 0)
                        .count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.soldKey = Sold.objKey AND (SELECT COUNT(*) FROM Client"
                + " WHERE Client.clientNumber = Sale.clientNumber AND Client.clientNumber = ?) > ?) > ?",
                db.sql());
        assertEquals(List.of(42, 0L, 0L), db.params());
    }

    // --- controls: these pass, and say where the line currently runs ----------

    /** A correlated sub-select with nothing captured works today. */
    @Test
    void correlationWithoutCapture() {
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> db.from(Sale.class).where(x -> x.soldKey() == s.objKey()).count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.soldKey = Sold.objKey) > ?", db.sql());
        assertEquals(List.of(0L), db.params());
    }

    /** A literal inside the nested lambda works, because it is in the bytecode. */
    @Test
    void literalInsideNestedLambda() {
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> db.from(Sale.class)
                        .where(x -> x.soldKey() == s.objKey() && x.clientNumber() == 42)
                        .count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.soldKey = Sold.objKey AND Sale.clientNumber = ?) > ?", db.sql());
        assertEquals(List.of(42, 0L), db.params());
    }

    /** The same captured value works in the outer lambda, which is the serializable one. */
    @Test
    void capturedValueInOuterLambda() {
        var clientNumber = 42;
        var db = new CapturingDatabase();

        db.from(Sold.class)
                .where(s -> s.objKey() == clientNumber
                        && db.from(Sale.class).where(x -> x.soldKey() == s.objKey()).count() > 0)
                .list();

        assertEquals("SELECT Sold.* FROM Sold WHERE Sold.objKey = ? AND (SELECT COUNT(*) FROM Sale"
                + " WHERE Sale.soldKey = Sold.objKey) > ?", db.sql());
        assertEquals(List.of(42, 0L), db.params());
    }

    /** And a field read works in the outer lambda too, for the same reason. */
    @Test
    void instanceFieldInOuterLambda() {
        var db = new CapturingDatabase();

        db.from(Sold.class).where(s -> s.objKey() == this.mutableField).list();

        assertEquals("SELECT Sold.* FROM Sold WHERE Sold.objKey = ?", db.sql());
        assertEquals(List.of(42), db.params());
    }
}
