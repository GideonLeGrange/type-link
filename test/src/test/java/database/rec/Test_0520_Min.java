package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0520_Min extends DatabaseTest {

    @TestTemplate
    public void testMin(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(amount) FROM Invoice", from(testDb, Invoice.class)
                .min(Invoice::amount), Double.class);

    }

    @TestTemplate
    public void testMinWithAdd(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(amount + id) FROM Invoice", from(testDb, Invoice.class)
                .min(invoice -> invoice.amount() + invoice.id()), Double.class);
    }

    @TestTemplate
    public void testMinWithMultiply(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(amount * id) FROM Invoice", from(testDb, Invoice.class)
                .min(invoice -> invoice.amount() * invoice.id()), Double.class);
    }

    @TestTemplate
    public void testMinWithSubtract(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(amount -100) FROM Invoice", from(testDb, Invoice.class)
                .min(invoice -> invoice.amount() - 100), Double.class);
    }

    @TestTemplate
    public void testMinWithDivide(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(amount / 2) FROM Invoice", from(testDb, Invoice.class)
                .min(invoice -> invoice.amount() / 2), Double.class);
    }

    @TestTemplate
    public void testJoinMin(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(amount) FROM Invoice JOIN Client ON Invoice.clientId=Client.id WHERE Client.name='Acme Corp'",
                from(testDb, Invoice.class).join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().equals("Acme Corp"))
                        .min((i, _) -> i.amount()), Double.class);

    }

    @TestTemplate
    public void testJoinJoinMin(TestDatabase testDb) throws SQLException {
        testNumber(testDb, """
                        SELECT MIN(amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Person.clientId=Client.id
                        WHERE Person.name = 'Bob Jones'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.name().equals("Bob Jones"))
                        .min((i, _, _) -> i.amount()), Double.class);
    }


}
