package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_2800_Limit extends DatabaseTest {

    @TestTemplate
    public void testJoinWithLimit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Invoice.id
                        LIMIT 3""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((i, _) -> i.id())
                        .limit(3)
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testJoinWithLimitAndOffset(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Invoice.id
                        LIMIT 3 OFFSET 2""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((i, _) -> i.id())
                        .limit(3, 2)
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testJoinWithLimitAndWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        WHERE Invoice.amount > 100
                        ORDER BY Invoice.amount
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((i, _) -> i.amount() > 100)
                        .orderBy((i, _) -> i.amount())
                        .limit(5)
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testJoinWithLimitMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name, Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name, Invoice.amount
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((_, c) -> c.name())
                        .thenBy((i, _) -> i.amount())
                        .limit(5)
                        .list((_, c) -> c.name(), (i, _) -> i.amount()),
                String.class, Double.class);
    }

    @TestTemplate
    public void testJoinWithLimitOffsetMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name, Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name, Invoice.amount
                        LIMIT 3 OFFSET 2""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((_, c) -> c.name())
                        .thenBy((i, _) -> i.amount())
                        .limit(3, 2)
                        .list((_, c) -> c.name(), (i, _) -> i.amount()),
                String.class, Double.class);
    }

    @TestTemplate
    public void testJoinWithLimitDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Invoice.amount DESC
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderByDescending((i, _) -> i.amount())
                        .limit(5)
                        .list((i, _) -> i),
                Invoice.class);
    }

}

