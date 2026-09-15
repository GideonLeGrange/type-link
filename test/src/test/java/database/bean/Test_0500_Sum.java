package database.bean;

import bean.Client;
import bean.Invoice;
import bean.Person;
import database.testing.BeanDatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0500_Sum extends BeanDatabaseTest {

    @TestTemplate
    public void testSum(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount) FROM Invoice", from(testDb, Invoice.class)
                .sum(Invoice::getAmount), Double.class);

    }

    @TestTemplate
    public void testSumWithAdd(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount + id) FROM Invoice", from(testDb, Invoice.class)
                .sum(invoice -> invoice.getAmount() + invoice.getId()), Double.class);
    }

    @TestTemplate
    public void testSumWithMultiply(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount * id) FROM Invoice", from(testDb, Invoice.class)
                .sum(invoice -> invoice.getAmount() * invoice.getId()), Double.class);
    }

    @TestTemplate
    public void testSumWithSubtract(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount -100) FROM Invoice", from(testDb, Invoice.class)
                .sum(invoice -> invoice.getAmount() - 100), Double.class);
    }

    @TestTemplate
    public void testSumWithDivide(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount / 2) FROM Invoice", from(testDb, Invoice.class)
                .sum(invoice -> invoice.getAmount() / 2), Double.class);
    }

    @TestTemplate
    public void testJoinSum(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount) FROM Invoice JOIN Client ON Invoice.clientId=Client.id WHERE Client.name='Acme Corp'",
                from(testDb, Invoice.class).join(Client.class, (i, c) -> i.getClientId().equals(c.getId()))
                        .where((_, c) -> c.getName().equals("Acme Corp"))
                        .sum((i, _) -> i.getAmount()), Double.class);

    }

    @TestTemplate
    public void testJoinJoinSum(TestDatabase testDb) throws SQLException {
        testNumber(testDb, """
                        SELECT SUM(amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Person.clientId=Client.id
                        WHERE Person.name = 'Bob Jones'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.getClientId().equals(c.getId()))
                        .join(Person.class, (_, c, p) -> c.getId().equals(p.getClientId()))
                        .where((_, _, p) -> p.getName().equals("Bob Jones"))
                        .sum((i, _, _) -> i.getAmount()), Double.class);
    }

}
