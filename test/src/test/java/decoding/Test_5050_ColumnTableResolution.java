package decoding;

import me.legrange.typelink.QueryPredicate1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * How a column's table is decided, and the two places the current answer is wrong.
 *
 * <p>A column reaches the resolver by one of two routes - an accessor call ({@code c.getId()}) or a
 * direct field read ({@code c.id}) - and each route decides the table differently:
 *
 * <ul>
 *   <li>{@code ColumnResolver.methodCall} uses the <em>declared type of the lambda argument</em>.
 *   <li>{@code ColumnResolver.columnForField} uses <em>the single table in context</em>, where
 *       there is exactly one, and the argument's declared type otherwise.
 * </ul>
 *
 * <p>Neither rule is right, and each is wrong where the other is right. The declared type is wrong
 * when a predicate is declared over a supertype, because then it names a table that is not in the
 * query. The single-table-in-context rule is wrong inside a correlated sub-select, because the
 * enclosing row's columns do not belong to the inner scope's table.
 *
 * <p>The rule that covers both: a lambda's own parameters resolve <em>positionally</em> against the
 * tables of the scope they belong to, and anything captured from an enclosing scope keeps its
 * declared type.
 *
 * <p><b>Four of these tests fail as written.</b> They are the specification for that change, not a
 * report of something already working. {@link #accessorAndFieldAgree} is the one worth reading
 * first: it asserts only that the two routes give the same answer, without taking a view on which,
 * and it fails.
 *
 * <p><b>Which failure matters.</b> {@link #accessorOnSupertypeLambda} is the one on anyone's path.
 * A predicate declared over a supertype is what a caller must write whenever the code holding it
 * does not know the concrete type - an access control rule that governs many unrelated types, for
 * instance - and today that produces SQL naming a table the query does not contain.
 *
 * <p>{@link #correlatedSubSelectViaField} is the more alarming failure to read, because it is not
 * an error but valid SQL comparing a column to itself, which runs and returns the wrong rows. It is
 * also the harder one to reach: it needs a field the lambda can read directly, and entities
 * generally keep their fields private, which sends the same expression down the accessor route
 * where correlation already works. Worth fixing, but not worth treating as an active hazard.
 */
class Test_5050_ColumnTableResolution {

    // Fields are reachable from a lambda in this class, which is what the field-access route needs;
    // the bean fixtures keep theirs private.
    public static class Entity {
    }

    public static class Client extends Entity {
        int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class Invoice extends Entity {
        int clientId;
        int amount;

        public int getClientId() {
            return clientId;
        }

        public void setClientId(int clientId) {
            this.clientId = clientId;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    private static CapturingBeanDatabase<Entity> db() {
        return new CapturingBeanDatabase<>(Entity.class);
    }

    // --- a lambda declared over the queried type: both routes already agree ---

    @Test
    void accessorOnSubtypeLambda() {
        var db = db();
        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> i.getClientId() == 7).list();

        assertEquals("Invoice.clientId = ?", db.where());
    }

    @Test
    void fieldOnSubtypeLambda() {
        var db = db();
        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> i.clientId == 7).list();

        assertEquals("Invoice.clientId = ?", db.where());
    }

    // --- a lambda declared over a supertype -----------------------------------

    /**
     * Fails: resolves to {@code Entity.clientId}, a table the query never names.
     *
     * <p>This is the shape an access control rule has to take when one rule governs many unrelated
     * types, which is the common case - a rule cannot be written against a type it does not know.
     */
    @Test
    void accessorOnSupertypeLambda() {
        QueryPredicate1<Entity> overSupertype = e -> ((Invoice) e).getClientId() == 7;

        var db = db();
        db.from(Invoice.class).where(narrow(overSupertype)).list();

        assertEquals("Invoice.clientId = ?", db.where());
    }

    /** Passes: the field route already prefers the table in context. */
    @Test
    void fieldOnSupertypeLambda() {
        QueryPredicate1<Entity> overSupertype = e -> ((Invoice) e).clientId == 7;

        var db = db();
        db.from(Invoice.class).where(narrow(overSupertype)).list();

        assertEquals("Invoice.clientId = ?", db.where());
    }

    /**
     * Fails. Asserts only that the two routes agree, without saying which is right - writing
     * {@code i.getClientId()} rather than {@code i.clientId} should not change the SQL.
     */
    @Test
    void accessorAndFieldAgree() {
        QueryPredicate1<Entity> viaAccessor = e -> ((Invoice) e).getClientId() == 7;
        QueryPredicate1<Entity> viaField = e -> ((Invoice) e).clientId == 7;

        var accessor = db();
        accessor.from(Invoice.class).where(narrow(viaAccessor)).list();
        var field = db();
        field.from(Invoice.class).where(narrow(viaField)).list();

        assertEquals(field.where(), accessor.where());
    }

    // --- a correlated sub-select ----------------------------------------------

    /** Passes: the declared type happens to be right for a captured row. */
    @Test
    void correlatedSubSelectViaAccessor() {
        var db = db();
        db.from(Client.class).list(Client::getId,
                c -> db.from(Invoice.class)
                        .where(i -> i.getClientId() == c.getId())
                        .sum(Invoice::getAmount));

        assertEquals("SELECT Client.id, (SELECT SUM(Invoice.amount) FROM Invoice"
                + " WHERE Invoice.clientId = Client.id) FROM Client", db.sql());
    }

    /**
     * Fails, and silently. Produces {@code Invoice.clientId = Invoice.id} - the enclosing
     * {@code Client} row's column is resolved against the inner scope's table, because the inner
     * scope has exactly one. That is valid SQL that runs and returns the wrong rows; nothing
     * throws, and no existing test covers it.
     */
    @Test
    void correlatedSubSelectViaField() {
        var db = db();
        db.from(Client.class).list(Client::getId,
                c -> db.from(Invoice.class)
                        .where(i -> i.getClientId() == c.id)
                        .sum(Invoice::getAmount));

        assertEquals("SELECT Client.id, (SELECT SUM(Invoice.amount) FROM Invoice"
                + " WHERE Invoice.clientId = Client.id) FROM Client", db.sql());
    }

    /** Fails for the same reason, stated as agreement rather than as a specific expectation. */
    @Test
    void correlatedAccessorAndFieldAgree() {
        var accessor = db();
        accessor.from(Client.class).list(Client::getId,
                c -> accessor.from(Invoice.class).where(i -> i.getClientId() == c.getId()).sum(Invoice::getAmount));

        var field = db();
        field.from(Client.class).list(Client::getId,
                c -> field.from(Invoice.class).where(i -> i.getClientId() == c.id).sum(Invoice::getAmount));

        assertEquals(accessor.sql(), field.sql());
    }

    /** The functional interface is not contravariant, and these tests do not need it to be. */
    @SuppressWarnings("unchecked")
    private static <X> QueryPredicate1<X> narrow(QueryPredicate1<? super X> predicate) {
        return (QueryPredicate1<X>) predicate;
    }
}
