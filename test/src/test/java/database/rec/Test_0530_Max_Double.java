package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0530_Max_Double extends DatabaseTest {

    @TestTemplate
    public void testMax(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(amount) FROM Invoice", from(testDb, Invoice.class)
                .max(Invoice::amount), Double.class);
    }

    @TestTemplate
    public void testMaxWithAdd(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(amount + id) FROM Invoice", from(testDb, Invoice.class)
                .max(invoice -> invoice.amount() + invoice.id()), Double.class);
    }

    @TestTemplate
    public void testMaxWithMultiply(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(amount * id) FROM Invoice", from(testDb, Invoice.class)
                .max(invoice -> invoice.amount() * invoice.id()), Double.class);
    }

    @TestTemplate
    public void testMaxWithSubtract(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(amount -100) FROM Invoice", from(testDb, Invoice.class)
                .max(invoice -> invoice.amount() - 100), Double.class);
    }

    @TestTemplate
    public void testMaxWithDivide(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(amount / 2) FROM Invoice", from(testDb, Invoice.class)
                .max(invoice -> invoice.amount() / 2), Double.class);
    }

    @TestTemplate
    public void testJoinMax(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(amount) FROM Invoice JOIN Client ON Invoice.clientId=Client.id WHERE Client.name='Acme Corp'",
                from(testDb, Invoice.class).join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().equals("Acme Corp"))
                        .max((i, _) -> i.amount()), Double.class);
    }

    @TestTemplate
    public void testJoinJoinMax(TestDatabase testDb) throws SQLException {
        testNumber(testDb, """
                        SELECT MAX(amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Person.clientId=Client.id
                        WHERE Person.name = 'Bob Jones'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.name().equals("Bob Jones"))
                        .max((i, _, _) -> i.amount()), Double.class);
    }

}
