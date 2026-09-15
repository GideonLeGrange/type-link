package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0531_Max_Int extends DatabaseTest {

    @TestTemplate
    public void testMax(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(age) FROM Person", from(testDb, Person.class)
                .max(Person::age), Integer.class);
    }

    @TestTemplate
    public void testMaxWithAdd(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(age + 1) FROM Person", from(testDb, Person.class)
                .max(person -> person.age() + 1), Integer.class);
    }

    @TestTemplate
    public void testMaxWithMultiply(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(age * 2) FROM Person", from(testDb, Person.class)
                .max(person -> person.age() * 2), Integer.class);
    }

    @TestTemplate
    public void testMaxWithSubtract(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(age - 5) FROM Person", from(testDb, Person.class)
                .max(person -> person.age() - 5), Integer.class);
    }

    @TestTemplate
    public void testMaxWithDivide(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(age / 2) FROM Person", from(testDb, Person.class)
                .max(person -> person.age() / 2), Integer.class);
    }

    @TestTemplate
    public void testJoinMax(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(age) FROM Person JOIN Client ON Person.clientId=Client.id WHERE Client.name='Acme Corp'",
                from(testDb, Person.class).join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().equals("Acme Corp"))
                        .max((i, _) -> i.age()), Integer.class);
    }


}
