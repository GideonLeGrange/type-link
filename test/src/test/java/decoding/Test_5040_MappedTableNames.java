package decoding;

import org.junit.jupiter.api.Test;
import rec.Client;
import rec.Invoice;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test: FROM and JOIN must name tables the way the mapper does.
 *
 * <p><b>The bug this guards.</b> {@code SqlGenerator} emitted FROM and JOIN using
 * {@code Class::getSimpleName} while columns were qualified with
 * {@code TableMapper.tableName(type)}. For the default mappers those are the same string, so
 * nothing showed. For a mapper that maps a type to a table of another name - which is the whole
 * point of {@code tableName} being on the interface - the two disagreed and the generated SQL
 * named two different tables at once:
 *
 * <pre>SELECT CLIENTS.name FROM Client</pre>
 *
 * <p>Custom table names were therefore not merely cosmetically wrong, they did not work at all.
 *
 * <p>The name is now resolved while parsing, where the mapper is in hand, and carried on
 * {@code SqlTableRef} - the same approach {@code SqlTableColumn} already took for columns.
 *
 * <p><b>Why this test can exist without touching the schema.</b> {@link CapturingDatabase} builds
 * the SQL without executing it, so nothing has to be renamed in the database for the assertion to
 * mean something. The rest of the suite cannot cover this: it uses the default mapper, where the
 * mapped name and the class name coincide.
 */
class Test_5040_MappedTableNames {

    private static final RenamingMapper RENAMED = new RenamingMapper(Map.of(
            Invoice.class, "INVOICE_HEADER",
            Client.class, "CUSTOMER"));

    @Test
    void fromUsesTheMappedName() {
        var db = new CapturingDatabase(RENAMED);
        db.from(Invoice.class).where(i -> i.paid()).list();

        assertEquals("SELECT INVOICE_HEADER.* FROM INVOICE_HEADER WHERE INVOICE_HEADER.paid <> ?", db.sql());
    }

    @Test
    void joinUsesTheMappedName() {
        var db = new CapturingDatabase(RENAMED);
        db.from(Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .list((i, _) -> i);

        assertEquals("SELECT INVOICE_HEADER.* FROM INVOICE_HEADER"
                + " INNER JOIN CUSTOMER ON INVOICE_HEADER.clientId = CUSTOMER.id", db.sql());
    }

    @Test
    void leftJoinUsesTheMappedName() {
        var db = new CapturingDatabase(RENAMED);
        db.from(Invoice.class)
                .leftJoin(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .list((i, _) -> i);

        assertEquals("SELECT INVOICE_HEADER.* FROM INVOICE_HEADER"
                + " LEFT OUTER JOIN CUSTOMER ON INVOICE_HEADER.clientId = CUSTOMER.id", db.sql());
    }

    @Test
    void multiTableFromUsesTheMappedNames() {
        var db = new CapturingDatabase(RENAMED);
        db.from(Invoice.class, Client.class)
                .where((i, c) -> i.clientId().equals(c.id()))
                .list((i, _) -> i);

        assertEquals("SELECT INVOICE_HEADER.* FROM INVOICE_HEADER, CUSTOMER"
                + " WHERE INVOICE_HEADER.clientId = CUSTOMER.id", db.sql());
    }

    @Test
    void subSelectUsesTheMappedName() {
        var db = new CapturingDatabase(RENAMED);
        db.from(Invoice.class)
                .where(i -> db.from(Client.class)
                        .where(c -> c.name().startsWith("Acme"))
                        .list(Client::id)
                        .contains(i.clientId()))
                .list();

        assertEquals("SELECT INVOICE_HEADER.* FROM INVOICE_HEADER"
                + " WHERE INVOICE_HEADER.clientId  IN (SELECT CUSTOMER.id FROM CUSTOMER"
                + " WHERE CUSTOMER.name LIKE ?)", db.sql());
    }

    /** A type the mapper does not rename still uses its class name, as before. */
    @Test
    void unmappedTypesAreUnaffected() {
        var db = new CapturingDatabase(new RenamingMapper(Map.of()));
        db.from(Invoice.class).where(i -> i.paid()).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.paid <> ?", db.sql());
    }
}
