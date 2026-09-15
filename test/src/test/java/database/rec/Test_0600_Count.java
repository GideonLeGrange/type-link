package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;


@SuppressWarnings("NewClassNamingConvention")
public final class Test_0600_Count extends DatabaseTest {

    @TestTemplate
    public void testCount(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT COUNT(id) FROM Invoice", from(testDb, Invoice.class)
                .count(Invoice::id), Long.class);
    }

    @TestTemplate
    public void testCountWithLambda(TestDatabase testDb) throws SQLException {
        //noinspection Convert2MethodRef
        testNumber(testDb, "SELECT COUNT(id) FROM Invoice", from(testDb, Invoice.class)
                .count(i -> i.id()), Long.class);
    }

    @TestTemplate
    public void testCountWhereNull(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT COUNT(birthDay) FROM Person", from(testDb, Person.class)
                .count(Person::birthDay), Long.class);
    }

    @TestTemplate
    public void testCountRows(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT COUNT(*) FROM Person", from(testDb, Person.class)
                .count(), Long.class);
    }

    @TestTemplate
    public void testCountWithLambdaJoin(TestDatabase testDb) throws SQLException {
        testNumber(testDb, """
                        SELECT COUNT(*) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        WHERE Client.name LIKE 'Acme%'""",
                db(testDb).from(Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().startsWith("Acme"))
                        .count(),
                Long.class);
    }

    @TestTemplate
    public void testCountWithLambdaJoinJoin(TestDatabase testDb) throws SQLException {
        testNumber(testDb, """
                        SELECT COUNT(*) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Person.clientId=Client.id
                        WHERE Person.name LIKE 'John%'""",
                db(testDb).from(Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.name().startsWith("John"))
                        .count(),
                Long.class);
    }

}
