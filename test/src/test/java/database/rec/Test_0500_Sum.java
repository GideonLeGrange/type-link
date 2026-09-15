package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0500_Sum extends DatabaseTest {

    @TestTemplate
    public void testSum(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount) FROM Invoice", from(testDb, Invoice.class)
                .sum(Invoice::amount), Double.class);

    }

    @TestTemplate
    public void testSumWithAdd(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount + id) FROM Invoice", from(testDb, Invoice.class)
                .sum(invoice -> invoice.amount() + invoice.id()), Double.class);
    }

    @TestTemplate
    public void testSumWithMultiply(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount * id) FROM Invoice", from(testDb, Invoice.class)
                .sum(invoice -> invoice.amount() * invoice.id()), Double.class);
    }

    @TestTemplate
    public void testSumWithSubtract(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount -100) FROM Invoice", from(testDb, Invoice.class)
                .sum(invoice -> invoice.amount() - 100), Double.class);
    }

    @TestTemplate
    public void testSumWithDivide(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount / 2) FROM Invoice", from(testDb, Invoice.class)
                .sum(invoice -> invoice.amount() / 2), Double.class);
    }

    @TestTemplate
    public void testJoinSum(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount) FROM Invoice JOIN Client ON Invoice.clientId=Client.id WHERE Client.name='Acme Corp'",
                from(testDb, Invoice.class).join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().equals("Acme Corp"))
                        .sum((i, _) -> i.amount()), Double.class);

    }

    @TestTemplate
    public void testJoinJoinSum(TestDatabase testDb) throws SQLException {
        testNumber(testDb, """
                        SELECT SUM(amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Person.clientId=Client.id
                        WHERE Person.name = 'Bob Jones'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.name().equals("Bob Jones"))
                        .sum((i, _, _) -> i.amount()), Double.class);
    }

}
