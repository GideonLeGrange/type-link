package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static me.legrange.typelink.Selects.sum;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_3400_OrderBy extends DatabaseTest {

    @TestTemplate
    public void testGroupByWithSumAndOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Person.email,SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        GROUP BY Person.email
                        ORDER BY SUM(Invoice.amount)""", from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .groupBy((_, _, p) -> p.email())
                        .orderBy((i, _, _) -> sum(i.amount()))
                        .list((_, _, p) -> p.email(),
                                (i, _, _) -> sum(i.amount())),
                String.class, Double.class);
    }


    @TestTemplate
    public void testGroupByWithSumAndOrderByDescending(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Person.email,SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        GROUP BY Person.email
                        ORDER BY SUM(Invoice.amount) DESC, Person.email""", from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .groupBy((_, _, p) -> p.email())
                        .orderByDescending((i, _, _) -> sum(i.amount()))
                        .thenBy((_, _, p) -> p.email())
                        .list((_, _, p) -> p.email(),
                                (i, _, _) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testOrderByAndThenBy(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name,Person.email,Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Client.name LIKE 'Acme%'
                        ORDER BY Client.name,Invoice.invoiceDate""", from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, c, _) -> c.name().startsWith("Acme"))
                        .orderBy((_, c, _) -> c.name())
                        .thenBy((i, _, _) -> i.invoiceDate())
                        .list((_, c, _) -> c.name(),
                                (_, _, p) -> p.email(), (i, _, _) -> i.amount()),
                String.class, String.class, Double.class);
    }

    @TestTemplate
    public void testOrderByThenByThenBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Client.name, Person.name, Invoice.amount""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((_, c, _) -> c.name())
                        .thenBy((_, _, p) -> p.name())
                        .thenBy((i, _, _) -> i.amount())
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByThenByDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Client.name, Invoice.amount DESC""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((_, c, _) -> c.name())
                        .thenByDescending((i, _, _) -> i.amount())
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByDescendingThenBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Person.age DESC, Invoice.invoiceDate""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderByDescending((_, _, p) -> p.age())
                        .thenBy((i, _, _) -> i.invoiceDate())
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByDescendingThenByDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Client.name DESC, Invoice.amount DESC""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderByDescending((_, c, _) -> c.name())
                        .thenByDescending((i, _, _) -> i.amount())
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByThenByWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Person.age > 20
                        ORDER BY Client.name, Invoice.amount""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.age() > 20)
                        .orderBy((_, c, _) -> c.name())
                        .thenBy((i, _, _) -> i.amount())
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByThenByWithMultipleColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                """
                        SELECT Client.name, Person.email, Invoice.amount FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Client.name, Person.email""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((_, c, _) -> c.name())
                        .thenBy((_, _, p) -> p.email())
                        .list((_, c, _) -> c.name(), (_, _, p) -> p.email(), (i, _, _) -> i.amount()),
                String.class, String.class, Double.class);
    }

    @TestTemplate
    public void testOrderByThenByThenByDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Client.name, Person.age, Invoice.amount DESC""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((_, c, _) -> c.name())
                        .thenBy((_, _, p) -> p.age())
                        .thenByDescending((i, _, _) -> i.amount())
                        .list((i, _, _) -> i),
                Invoice.class);
    }


}
