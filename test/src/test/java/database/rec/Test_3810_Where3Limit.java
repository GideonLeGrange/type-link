package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_3810_Where3Limit extends DatabaseTest {

    @TestTemplate
    public void testWhere3Limit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Invoice.amount > 100
                        LIMIT 3""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((i, _, _) -> i.amount() > 100)
                        .limit(3)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testWhere3LimitWithPersonFilter(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Person.name LIKE '%Bob%'
                        LIMIT 5""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.name().contains("Bob"))
                        .limit(5)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testWhere3LimitMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Invoice.id, Client.id, Person.id FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Person.name LIKE '%Bob%'
                        LIMIT 3""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.name().contains("Bob"))
                        .limit(3)
                        .list((i, _, _) -> i.id(), (_, c, _) -> c.id(), (_, _, p) -> p.id()),
                Long.class, Long.class, Long.class);
    }

    @TestTemplate
    public void testWhere3LimitWithOffset(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Invoice.amount > 100
                        LIMIT 3 OFFSET 2""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((i, _, _) -> i.amount() > 100)
                        .limit(3, 2)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testWhere3LimitOffsetMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Invoice.id, Client.id, Person.id FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Person.name LIKE '%Bob%'
                        LIMIT 3 OFFSET 1""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.name().contains("Bob"))
                        .limit(3, 1)
                        .list((i, _, _) -> i.id(), (_, c, _) -> c.id(), (_, _, p) -> p.id()),
                Long.class, Long.class, Long.class);
    }

}

