package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static me.legrange.typelink.Selects.sum;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_2400_OrderBy extends DatabaseTest {

    @TestTemplate
    public void testGroupByWithSumAndOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        GROUP BY Client.name
                        ORDER BY SUM(Invoice.amount)""", from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .groupBy((_, c) -> c.id())
                        .orderBy((i, _) -> sum(i.amount()))
                        .list((_, c) -> c.name(),
                                (i, _) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testGroupByWithSumAndOrderByDescending(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        GROUP BY Client.name
                        ORDER BY SUM(Invoice.amount) DESC""", from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .groupBy((_, c) -> c.id())
                        .orderByDescending((i, _) -> sum(i.amount()))
                        .list((_, c) -> c.name(),
                                (i, _) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testOrderByThenBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name, Invoice.amount""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((_, c) -> c.name())
                        .thenBy((i, _) -> i.amount())
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByThenByDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name, Invoice.amount DESC""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((_, c) -> c.name())
                        .thenByDescending((i, _) -> i.amount())
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByDescendingThenBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name DESC, Invoice.invoiceDate""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderByDescending((_, c) -> c.name())
                        .thenBy((i, _) -> i.invoiceDate())
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByDescendingThenByDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name DESC, Invoice.amount DESC""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderByDescending((_, c) -> c.name())
                        .thenByDescending((i, _) -> i.amount())
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByThenByWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        WHERE Invoice.amount > 100
                        ORDER BY Client.name, Invoice.invoiceDate""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((i, _) -> i.amount() > 100)
                        .orderBy((_, c) -> c.name())
                        .thenBy((i, _) -> i.invoiceDate())
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByThenByWithMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name, Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name, Invoice.amount""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((_, c) -> c.name())
                        .thenBy((i, _) -> i.amount())
                        .list((_, c) -> c.name(), (i, _) -> i.amount()),
                String.class, Double.class);
    }

    @TestTemplate
    public void testGroupByWithSumOrderByThenBy(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        GROUP BY Client.name
                        ORDER BY SUM(Invoice.amount) DESC, Client.name""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .groupBy((_, c) -> c.name())
                        .orderByDescending((i, _) -> sum(i.amount()))
                        .thenBy((_, c) -> c.name())
                        .list((_, c) -> c.name(),
                                (i, _) -> sum(i.amount())),
                String.class, Double.class);
    }

}
