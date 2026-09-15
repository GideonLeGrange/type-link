package decoding;

import me.legrange.typelink.QueryPredicate1;
import me.legrange.typelink.TableMapper;
import me.legrange.typelink.sql.parser.QueryParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Naming the enclosing scope's row from inside a sub-select, when its type says nothing.
 *
 * <p>A correlated sub-select has to refer to the row of the scope around it. Which table that row
 * belongs to has been read off its declared type, which works while the predicate is written over a
 * concrete type - and says nothing at all when it is written over a type variable, because the
 * parameter erases to the bound.
 *
 * <p>That is not an exotic case. It is what any rule written for many types looks like: it has a
 * {@code Class} and a type variable, never a concrete type to name. The tests here use a helper
 * generic in exactly that way.
 *
 * <p>What makes it worth a test rather than a bug report is that it does not throw. It resolves the
 * row to its bound and emits a clause naming a table the query never mentions - valid-looking SQL
 * that is simply about something else.
 *
 * <p>The rule that covers it is the one already used for a scope's own rows, one level out: a row
 * parameter resolves <em>positionally</em> against the tables of the scope it belongs to. The only
 * new thing needed is knowing which scope that is.
 */
class Test_6000_EnclosingScopeRows {

    public interface Entity {
    }

    /** The shape a rule reaches a shared member through, when it cannot name a concrete type. */
    public interface HasSale {
        int saleKey();
    }

    public record Invoice(int objKey, int saleKey, int amount) implements Entity, HasSale {
    }

    public record Sale(int saleKey, int clientId) implements Entity {
    }

    public record Client(int clientId, String name) implements Entity {
    }

    /** A second table with the same member, so the two can be told apart. */
    public record Refund(int refundKey, int saleKey) implements Entity, HasSale {
    }

    private static final TableMapper<Record> MAPPER = TableMapper.RECORD_MAPPER;
    private static final TableMapper<Record> BY_TYPE = new InterfaceMapper<>(Record.class);

    /**
     * The same rule again, reaching the member through the interface rather than by naming a
     * concrete type - which is all a rule governing many types can do.
     */
    private static <T extends Entity> QueryPredicate1<T> soldViaInterface(CapturingDatabase db, int clientId) {
        return row -> db.from(Sale.class)
                .where(sale -> sale.saleKey() == ((HasSale) row).saleKey() && sale.clientId() == clientId)
                .count() > 0;
    }

    /**
     * Fails. Reached through an interface the accessor is not recognised as a column at all, and
     * what comes back is the enclosing row expanded to every column of its table - a comparison
     * against a row rather than against the one column meant.
     */
    @Test
    void aMemberReachedThroughAnInterfaceIsStillAColumn() {
        var sql = render(QueryParser.parsePredicate(
                soldViaInterface(new CapturingDatabase(), 42), Invoice.class, BY_TYPE));

        assertEquals("(SELECT COUNT(*) FROM Sale WHERE Sale.saleKey = Invoice.saleKey"
                + " AND Sale.clientId = ?) > ?", sql);
    }

    /** Written over a type variable, which is the whole point - so the row erases to its bound. */
    private static <T extends Entity> QueryPredicate1<T> soldToClient(CapturingDatabase db, int clientId) {
        // row is a T, which erases to Entity - the cast is how a rule reaches a member it knows the
        // type has, and it tells the decoder nothing about which table the row came from.
        return row -> db.from(Sale.class)
                .where(sale -> sale.saleKey() == ((Invoice) row).saleKey() && sale.clientId() == clientId)
                .count() > 0;
    }

    /**
     * The same predicate over a different table, which is what a rule governing many types does. It
     * has to name the table it was applied to, and nothing in the predicate says which that is.
     */
    @Test
    void theSamePredicateOverAnotherTableNamesThatOne() {
        var db = new CapturingDatabase();

        assertTrue(render(QueryParser.parsePredicate(soldToClient(db, 42), Invoice.class, MAPPER))
                .contains("Sale.saleKey = Invoice.saleKey"));
        assertTrue(render(QueryParser.parsePredicate(soldToClient(db, 42), Refund.class, MAPPER))
                .contains("Sale.saleKey = Refund.saleKey"));
    }

    @Test
    void theEnclosingRowNamesTheTableTheQueryIsOver() {
        var clause = QueryParser.parsePredicate(
                soldToClient(new CapturingDatabase(), 42), Invoice.class, MAPPER);

        assertEquals("(SELECT COUNT(*) FROM Sale WHERE Sale.saleKey = Invoice.saleKey"
                + " AND Sale.clientId = ?) > ?", render(clause));
    }

    /** Written over a concrete type it always worked, and has to keep working. */
    @Test
    void aConcretelyTypedPredicateIsUnchanged() {
        var db = new CapturingDatabase();
        QueryPredicate1<Invoice> concrete = row -> db.from(Sale.class)
                .where(sale -> sale.saleKey() == row.saleKey())
                .count() > 0;

        assertTrue(render(QueryParser.parsePredicate(concrete, Invoice.class, MAPPER))
                        .contains("Sale.saleKey = Invoice.saleKey"),
                "got: " + render(QueryParser.parsePredicate(concrete, Invoice.class, MAPPER)));
    }

    /** Two levels out: the innermost row still has to find the scope it came from. */
    @Test
    void aRowTwoScopesOutIsStillFound() {
        var db = new CapturingDatabase();
        QueryPredicate1<Invoice> nested = row -> db.from(Sale.class)
                .where(sale -> db.from(Client.class)
                        .where(client -> client.clientId() == sale.clientId()
                                && client.clientId() == row.objKey())
                        .count() > 0)
                .count() > 0;

        var sql = render(QueryParser.parsePredicate(nested, Invoice.class, MAPPER));

        assertTrue(sql.contains("Client.clientId = Sale.clientId"), "one out: " + sql);
        assertTrue(sql.contains("Client.clientId = Invoice.objKey"), "two out: " + sql);
    }

    /**
     * A row that was captured rather than being a row of any scope here.
     *
     * <p>This is what the same predicate looks like when it is run rather than compiled: the rule
     * evaluates itself against one object, the object is captured by the query it builds, and its
     * members are values to bind - not columns of anything. Told apart only by the declared type,
     * an erased row is indistinguishable from a table, and the read becomes a column of a table the
     * query has never heard of.
     */
    @Test
    void acapturedRowIsAValueAndNotATable() {
        Entity captured = new Invoice(1, 5, 100);
        var db = new CapturingDatabase(BY_TYPE);

        db.from(Sale.class).where(sale -> sale.saleKey() == ((Invoice) captured).saleKey()).list();

        assertEquals("SELECT Sale.* FROM Sale WHERE Sale.saleKey = ?", db.sql());
        assertEquals(java.util.List.of(5), db.params());
    }

    private static String render(me.legrange.typelink.sql.structure.SqlClause clause) {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> i.amount() == 0).list();
        return me.legrange.typelink.sql.generator.SqlGenerator.generate(
                new me.legrange.typelink.sql.structure.SqlQuery(
                        db.captured().select(), db.captured().from(), db.captured().joins(),
                        new me.legrange.typelink.sql.structure.SqlWhere(java.util.List.of(clause)),
                        db.captured().groupBy(), db.captured().having(),
                        db.captured().order(), db.captured().limit())).sql()
                .replaceFirst("^.*?WHERE ", "");
    }
}
