package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_2810_Where2Limit extends DatabaseTest {

    @TestTemplate
    public void testWhere2Limit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        WHERE Invoice.amount > 100
                        LIMIT 3""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((i, _) -> i.amount() > 100)
                        .limit(3)
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testWhere2LimitWithOffset(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        WHERE Invoice.amount > 100
                        LIMIT 3 OFFSET 2""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((i, _) -> i.amount() > 100)
                        .limit(3, 2)
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testWhere2LimitMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name, Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        WHERE Invoice.amount > 50
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((i, _) -> i.amount() > 50)
                        .limit(5)
                        .list((_, c) -> c.name(), (i, _) -> i.amount()),
                String.class, Double.class);
    }

    @TestTemplate
    public void testWhere2LimitOffsetMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name, Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        WHERE Invoice.amount > 50
                        LIMIT 3 OFFSET 1""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((i, _) -> i.amount() > 50)
                        .limit(3, 1)
                        .list((_, c) -> c.name(), (i, _) -> i.amount()),
                String.class, Double.class);
    }

}

