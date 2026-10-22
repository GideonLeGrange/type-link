package decoding;

import me.legrange.typelink.QueryPredicate1;
import org.junit.jupiter.api.Test;
import rec.Invoice;
import rec.Ledger;
import rec.Person;
import rec.Town;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test: comparisons against zero, and the literals that have their own opcode.
 *
 * <p><b>The bugs this guards.</b> Two, with one symptom each.
 *
 * <p>{@code constant()} did not list {@code DCONST_0}, {@code DCONST_1}, the {@code FCONST_}
 * opcodes or {@code ICONST_M1}. The compiler emits those in place of a constant pool entry, so
 * {@code amount > 0.0} failed to decode while {@code amount > 2.0} was fine.
 *
 * <p>{@code LCMP} and the float and double comparisons are treated as no-ops that leave both
 * operands on the stack, and the branch that follows popped two. A comparison of an {@code int}
 * against zero has no such instruction in front of it - the compiler emits a bare {@code IFxx}
 * against an implicit zero - so the second pop emptied the stack and {@code age == 0} failed while
 * {@code age == 1} worked.
 *
 * <p><b>Why these assert the SQL text.</b> The first attempt at the fix put the synthesised zero on
 * the wrong side, turning {@code age > 0} into {@code 0 > age}. Everything still decoded, and a
 * test that only checked a query came out would have passed. Equality is symmetric so it looked
 * correct; every ordered comparison was inverted. In a rights predicate that inversion turns "rows
 * you own" into "rows you do not", so assert the operator and the operand order, not just success.
 *
 * <p>Primitive columns are what make this matter in practice: ObjDB maps its key, int and link
 * fields to primitive {@code int}, and {@code == 0} is the idiomatic test for an unset link or an
 * unsaved object.
 */
class Test_5030_ComparisonOperands {

    // --- primitive int against zero: the bare-branch path ---------------------

    @Test
    void intEqualToZero() {
        assertPerson(p -> p.age() == 0, "Person.age = ?", 0);
    }

    @Test
    void intNotEqualToZero() {
        assertPerson(p -> p.age() != 0, "Person.age <> ?", 0);
    }

    @Test
    void intGreaterThanZero() {
        assertPerson(p -> p.age() > 0, "Person.age > ?", 0);
    }

    @Test
    void intGreaterThanOrEqualToZero() {
        assertPerson(p -> p.age() >= 0, "Person.age >= ?", 0);
    }

    @Test
    void intLessThanZero() {
        assertPerson(p -> p.age() < 0, "Person.age < ?", 0);
    }

    @Test
    void intLessThanOrEqualToZero() {
        assertPerson(p -> p.age() <= 0, "Person.age <= ?", 0);
    }

    /** Control: a non-zero literal takes the ordinary two-operand path and always worked. */
    @Test
    void intAgainstNonZero() {
        assertPerson(p -> p.age() > 18, "Person.age > ?", 18);
    }

    // --- literals with their own opcode ---------------------------------------

    @Test
    void doubleAgainstZeroLiteral() {
        assertInvoice(i -> i.amount() > 0.0, "Invoice.amount > ?", 0.0);
    }

    @Test
    void doubleAgainstOneLiteral() {
        assertInvoice(i -> i.amount() > 1.0, "Invoice.amount > ?", 1.0);
    }

    /** Control: anything else comes from the constant pool and always worked. */
    @Test
    void doubleAgainstOtherLiteral() {
        assertInvoice(i -> i.amount() > 2.5, "Invoice.amount > ?", 2.5);
    }

    @Test
    void floatAgainstZeroLiteral() {
        var db = new CapturingDatabase();
        db.from(Town.class).where((QueryPredicate1<Town>) t -> t.alt() > 0.0f).list();

        assertEquals("SELECT Town.id, Town.name, Town.alt FROM Town WHERE Town.alt > ?", db.sql());
    }

    // --- the comparison-instruction path, which must keep working --------------

    @Test
    void boxedLongAgainstZero() {
        assertInvoice(i -> i.clientId() == 0, "Invoice.clientId = ?", 0L);
    }

    /**
     * A double column compared against a captured int. The compiler widens the capture with
     * {@code I2D}, and conversions are discarded during decoding, so the operand still models as an
     * Integer while the stack holds a double. An early version of the fix decided between the two
     * branch shapes by operand type and broke exactly this - hence reading the instruction stream
     * instead.
     */
    @Test
    void doubleColumnAgainstWidenedIntCapture() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(amountAbove(7)).list();

        assertEquals("SELECT Invoice.id, Invoice.clientId, Invoice.invoiceDate, Invoice.description, Invoice.amount, Invoice.paid FROM Invoice WHERE Invoice.amount > ?", db.sql());
        assertEquals(List.of(7), db.params());
    }

    /** Two columns of different width, compared to each other rather than to a literal. */
    @Test
    void columnAgainstColumn() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> i.amount() > i.id()).list();

        assertEquals("SELECT Invoice.id, Invoice.clientId, Invoice.invoiceDate, Invoice.description, Invoice.amount, Invoice.paid FROM Invoice WHERE Invoice.amount > Invoice.id", db.sql());
    }

    // --- compareTo: the only way to order BigDecimal/BigInteger, which have no < or > -----------

    /**
     * {@code x.compareTo(y) > 0} decodes through the very same implicit-zero branch path as
     * {@code age > 0} above - {@code compareTo} is just another int-returning method call to the
     * decoder. Asserting operand order here guards against the same inversion this whole file
     * guards against for primitives: a wrong fix could turn {@code amount > y} into {@code y > amount}.
     */
    @Test
    void compareToGreaterThanZero() {
        assertLedger(l -> l.amount().compareTo(THRESHOLD) > 0, "Ledger.amount > ?", THRESHOLD);
    }

    @Test
    void compareToLessThanZero() {
        assertLedger(l -> l.amount().compareTo(THRESHOLD) < 0, "Ledger.amount < ?", THRESHOLD);
    }

    /** The reversed source form - zero on the left - must flip the operator, not just decode. */
    @Test
    void zeroLessThanCompareTo() {
        assertLedger(l -> 0 < l.amount().compareTo(THRESHOLD), "Ledger.amount > ?", THRESHOLD);
    }

    @Test
    void zeroGreaterThanCompareTo() {
        assertLedger(l -> 0 > l.amount().compareTo(THRESHOLD), "Ledger.amount < ?", THRESHOLD);
    }

    @Test
    void compareToEqualToZero() {
        assertLedger(l -> l.amount().compareTo(THRESHOLD) == 0, "Ledger.amount = ?", THRESHOLD);
    }

    @Test
    void bigIntegerCompareToGreaterThanZero() {
        var threshold = BigInteger.valueOf(1000);
        var db = new CapturingDatabase();
        db.from(Ledger.class).where((QueryPredicate1<Ledger>) l -> l.serial().compareTo(threshold) > 0).list();

        assertEquals(LEDGER_SELECT + "Ledger.serial > ?", db.sql());
        // SqlGenerator always binds BigDecimal for a DECIMAL column - drivers disagree on BigInteger.
        assertEquals(List.of(new BigDecimal(threshold)), db.params());
    }

    // --- helpers ---------------------------------------------------------------

    private static final BigDecimal THRESHOLD = new BigDecimal("50.00");

    private static final String LEDGER_SELECT = "SELECT Ledger.id, Ledger.opens, Ledger.amount, Ledger.serial, "
            + "Ledger.copies, Ledger.level, Ledger.initial, Ledger.grade FROM Ledger WHERE ";

    private static void assertLedger(QueryPredicate1<Ledger> predicate, String where, Object param) {
        var db = new CapturingDatabase();
        db.from(Ledger.class).where(predicate).list();

        assertEquals(LEDGER_SELECT + where, db.sql());
        assertEquals(List.of(param), db.params());
    }

    private static QueryPredicate1<Invoice> amountAbove(int threshold) {
        return i -> i.amount() > threshold;
    }

    private static void assertPerson(QueryPredicate1<Person> predicate, String where, Object param) {
        var db = new CapturingDatabase();
        db.from(Person.class).where(predicate).list();

        assertEquals("SELECT Person.id, Person.clientId, Person.name, Person.age, Person.email, Person.sex, Person.birthDay FROM Person WHERE " + where, db.sql());
        assertEquals(List.of(param), db.params());
    }

    private static void assertInvoice(QueryPredicate1<Invoice> predicate, String where, Object param) {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(predicate).list();

        assertEquals("SELECT Invoice.id, Invoice.clientId, Invoice.invoiceDate, Invoice.description, Invoice.amount, Invoice.paid FROM Invoice WHERE " + where, db.sql());
        assertEquals(List.of(param), db.params());
    }
}
