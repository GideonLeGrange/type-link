package decoding;

import me.legrange.typelink.QueryPredicate1;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Naming a column when the method alone does not say which one it is.
 *
 * <p>{@code TableMapper} already accepts that a table cannot be identified without its type -
 * {@code tableName} takes one. It then assumes a column can be identified by its method or field
 * alone. That holds for a mapper where every accessor belongs to exactly one table, and fails for an
 * ORM where a base class offers a generic way to reach a member each subtype names for itself.
 *
 * <p>ObjDB is such an ORM. {@code Obj.getObjKey()} is declared once, on the base class, and reads
 * whichever field that type declared as its key - {@code clientNumber} for one table,
 * {@code systemUserNumber} for another. Asked about the method, a mapper sees only
 * {@code Obj.getObjKey}; asked about the method <em>and the type</em>, it can answer.
 *
 * <p>The type is not new information the resolver has to go and find. It already has it: the same
 * expression that names the column has just called {@code tableName} with the resolved type, one
 * argument earlier. It simply was not passed on.
 *
 * <p>{@link KeyedMapper} stands in for that ORM here. The two tests over it fail as written; the
 * rest are controls, and the last one matters most - a mapper that implements only the older
 * method-only form keeps working, because the type-aware calls default to it.
 */
class Test_5080_TypeAwareColumnNames {

    public static class Entity {
        int key;

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }
    }

    public static class Client extends Entity {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Invoice extends Entity {
        int amount;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    private static CapturingBeanDatabase<Entity> db() {
        return new CapturingBeanDatabase<>(Entity.class, new KeyedMapper<>(Entity.class));
    }

    /** Fails: the generic accessor has to resolve to the queried table's own key column. */
    @Test
    void inheritedAccessorNamesTheSubtypesColumn() {
        var db = db();

        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> i.getKey() == 7).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.invoiceId = ?", db.sql());
        assertEquals(List.of(7), db.params());
    }

    /** Fails for the same reason by the other route, which reaches a different call site. */
    @Test
    void inheritedFieldNamesTheSubtypesColumn() {
        var db = db();

        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> i.key == 7).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.invoiceId = ?", db.sql());
        assertEquals(List.of(7), db.params());
    }

    /** Fails: the same accessor, a different table, a different column. */
    @Test
    void theSameAccessorNamesADifferentColumnInAnotherTable() {
        var db = db();

        db.from(Client.class).where((QueryPredicate1<Client>) c -> c.getKey() == 7).list();

        assertEquals("SELECT Client.* FROM Client WHERE Client.clientId = ?", db.sql());
    }

    /** Fails: correlating on the generic key is what an access rule over many types needs. */
    @Test
    void correlatingOnTheGenericKey() {
        var db = db();

        db.from(Client.class)
                .where((QueryPredicate1<Client>) c -> db.from(Invoice.class)
                        .where(i -> i.getAmount() == c.getKey())
                        .count() > 0)
                .list();

        assertEquals("SELECT Client.* FROM Client WHERE (SELECT COUNT(*) FROM Invoice"
                + " WHERE Invoice.amount = Client.clientId) > ?", db.sql());
    }

    // --- controls --------------------------------------------------------------

    /** An ordinary accessor is unaffected. */
    @Test
    void ordinaryAccessorIsUnchanged() {
        var db = db();

        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> i.getAmount() == 7).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.amount = ?", db.sql());
    }

    /**
     * A mapper that implements only the method-only form still works, because the type-aware calls
     * default to it. Every existing mapper is in this position, so this is the compatibility test.
     */
    @Test
    void aMapperWithNoTypeAwareOverridesStillWorks() {
        var db = new CapturingBeanDatabase<>(Entity.class);

        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> i.getAmount() == 7).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.amount = ?", db.sql());
    }
}
