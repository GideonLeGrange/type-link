package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0710_Join_Join extends DatabaseTest {

    @TestTemplate
    public void testDoubleJoin(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                SELECT Invoice.* FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .list((invoice, _, _) -> invoice), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleJoinWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                SELECT Invoice.* FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                WHERE Person.name LIKE '%Bob%'
                """,
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                        .where((_, _, person) -> person.name().contains("Bob"))
                        .list((invoice, _, _) -> invoice),
                Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleJoinWithComplexWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                SELECT Invoice.* FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                WHERE Person.name LIKE '%Bob%' AND Invoice.amount < 500
                """,
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                        .where((invoice, _, person) -> person.name().contains("Bob") && invoice.amount() < 500)
                        .list((invoice, _, _) -> invoice),
                Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleJoinWithOrWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                SELECT Invoice.* FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                WHERE Person.name LIKE '%Bob%' OR Invoice.amount > 900
                """,
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                        .where((invoice, _, person) -> person.name().contains("Bob") || invoice.amount() > 900)
                        .list((invoice, _, _) -> invoice),
                Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleJoinWithResultsFromAllTables(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                SELECT Invoice.id, Client.id, Person.id FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                WHERE Person.name LIKE '%Bob%'
                """,
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                        .where((_, _, person) -> person.name().contains("Bob"))
                        .list((invoice, _, _) -> invoice.id(), (_, client, _) -> client.id(), (_, _, person) -> person.id()),
                Long.class, Long.class, Long.class);
    }

}
