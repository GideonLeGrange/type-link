package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_3800_Limit extends DatabaseTest {

    @TestTemplate
    public void testDoubleJoinWithLimit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Invoice.id
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((i, _, _) -> i.id())
                        .limit(5)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testDoubleJoinWithLimitAndOffset(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Invoice.id
                        LIMIT 3 OFFSET 2""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((i, _, _) -> i.id())
                        .limit(3, 2)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testDoubleJoinWithLimitAndWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Person.age > 20
                        ORDER BY Invoice.amount
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.age() > 20)
                        .orderBy((i, _, _) -> i.amount())
                        .limit(5)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testDoubleJoinWithLimitMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name, Person.email, Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Client.name, Person.email
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((_, c, _) -> c.name())
                        .thenBy((_, _, p) -> p.email())
                        .limit(5)
                        .list((_, c, _) -> c.name(), (_, _, p) -> p.email(), (i, _, _) -> i.amount()),
                String.class, String.class, Double.class);
    }

    @TestTemplate
    public void testDoubleJoinWithLimitOffsetMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name, Person.name, Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Client.name, Invoice.amount
                        LIMIT 3 OFFSET 2""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((_, c, _) -> c.name())
                        .thenBy((i, _, _) -> i.amount())
                        .limit(3, 2)
                        .list((_, c, _) -> c.name(), (_, _, p) -> p.name(), (i, _, _) -> i.amount()),
                String.class, String.class, Double.class);
    }

    @TestTemplate
    public void testDoubleJoinWithLimitDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Invoice.amount DESC
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderByDescending((i, _, _) -> i.amount())
                        .limit(5)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testDoubleJoinWithLimitComplexOrder(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Client.name DESC, Person.age, Invoice.amount DESC
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderByDescending((_, c, _) -> c.name())
                        .thenBy((_, _, p) -> p.age())
                        .thenByDescending((i, _, _) -> i.amount())
                        .limit(5)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

}

