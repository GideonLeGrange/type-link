package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0510_Avg extends DatabaseTest {

    @TestTemplate
    public void testAvg(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT AVG(amount) FROM Invoice", from(testDb, Invoice.class)
                .avg(Invoice::amount), Double.class);

    }

    @TestTemplate
    public void testAvgWithAdd(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT AVG(amount + id) FROM Invoice", from(testDb, Invoice.class)
                .avg(invoice -> invoice.amount() + invoice.id()), Double.class);
    }

    @TestTemplate
    public void testAvgWithMultiply(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT AVG(amount * id) FROM Invoice", from(testDb, Invoice.class)
                .avg(invoice -> invoice.amount() * invoice.id()), Double.class);
    }

    @TestTemplate
    public void testAvgWithSubtract(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT AVG(amount -100) FROM Invoice", from(testDb, Invoice.class)
                .avg(invoice -> invoice.amount() - 100), Double.class);
    }

    @TestTemplate
    public void testAvgWithDivide(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT AVG(amount / 2) FROM Invoice", from(testDb, Invoice.class)
                .avg(invoice -> invoice.amount() / 2), Double.class);
    }

    @TestTemplate
    public void testJoinAvg(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT AVG(amount) FROM Invoice JOIN Client ON Invoice.clientId=Client.id WHERE Client.name='Acme Corp'",
                from(testDb, Invoice.class).join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().equals("Acme Corp"))
                        .avg((i, _) -> i.amount()), Double.class);

    }

    @TestTemplate
    public void testJoinJoinMax(TestDatabase testDb) throws SQLException {
        testNumber(testDb, """
                        SELECT AVG(amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Person.clientId=Client.id
                        WHERE Person.name = 'Bob Jones'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.name().equals("Bob Jones"))
                        .avg((i, _, _) -> i.amount()), Double.class);
    }

    @TestTemplate
    public void testAvgWithSubQuery(TestDatabase testDb) throws SQLException {
        var db = db(testDb);
        var testData = db.from(Invoice.class)
                .where(i -> i.amount() >
                        db.from(Invoice.class).avg(Invoice::amount))
                .list();
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        WHERE Invoice.amount > (SELECT AVG(amount) FROM Invoice)""",
                testData, Invoice.class);
    }

}
