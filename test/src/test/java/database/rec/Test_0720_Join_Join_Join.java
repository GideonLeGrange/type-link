package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Client;
import rec.Invoice;
import rec.Meeting;
import rec.Person;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0720_Join_Join_Join extends DatabaseTest {

    @TestTemplate
    public void testTripleJoin(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                SELECT Invoice.* FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                JOIN Meeting ON Client.id=Meeting.id
                ORDER BY Invoice.id, Person.id
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .join(Meeting.class, (_, client, _, meeting) -> client.id().equals(meeting.id()))
                .orderBy((invoice, _, _, _) -> invoice.id())
                .thenBy((_, _, person, _) -> person.id())
                .list((invoice, _, _, _) -> invoice), Invoice.class
        );
    }

    @TestTemplate
    public void testTripleJoinWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                SELECT Invoice.* FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                JOIN Meeting ON Client.id=Meeting.id
                WHERE Person.name LIKE '%Bob%' AND Meeting.subject LIKE '%Acme%'
                ORDER BY Invoice.id
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .join(Meeting.class, (_, client, _, meeting) -> client.id().equals(meeting.id()))
                .where((_, _, person, meeting) -> person.name().contains("Bob") && meeting.subject().contains("Acme"))
                .orderBy((invoice, _, _, _) -> invoice.id())
                .list((invoice, _, _, _) -> invoice), Invoice.class
        );
    }

    @TestTemplate
    public void testTripleJoinWithOrWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                SELECT Invoice.* FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                JOIN Meeting ON Client.id=Meeting.id
                WHERE Person.name LIKE '%Bob%' OR Meeting.subject LIKE '%Coyote%'
                ORDER BY Invoice.id, Person.id
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .join(Meeting.class, (_, client, _, meeting) -> client.id().equals(meeting.id()))
                .where((_, _, person, _) -> person.name().contains("Bob"))
                .or((_, _, _, meeting) -> meeting.subject().contains("Coyote"))
                .orderBy((invoice, _, _, _) -> invoice.id())
                .thenBy((_, _, person, _) -> person.id())
                .list((invoice, _, _, _) -> invoice), Invoice.class
        );
    }

    @TestTemplate
    public void testTripleJoinWithResultsFromAllTables(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                SELECT Invoice.id, Client.id, Person.id, Meeting.id FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                JOIN Meeting ON Client.id=Meeting.id
                ORDER BY Invoice.id, Person.id
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .join(Meeting.class, (_, client, _, meeting) -> client.id().equals(meeting.id()))
                .orderBy((invoice, _, _, _) -> invoice.id())
                .thenBy((_, _, person, _) -> person.id())
                .list((invoice, _, _, _) -> invoice.id(), (_, client, _, _) -> client.id(),
                        (_, _, person, _) -> person.id(), (_, _, _, meeting) -> meeting.id()),
                Long.class, Long.class, Long.class, Long.class);
    }

    @TestTemplate
    public void testTripleJoinWholeRows(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                SELECT Invoice.*, Client.*, Person.*, Meeting.* FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                JOIN Meeting ON Client.id=Meeting.id
                ORDER BY Invoice.id, Person.id
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .join(Meeting.class, (_, client, _, meeting) -> client.id().equals(meeting.id()))
                .orderBy((invoice, _, _, _) -> invoice.id())
                .thenBy((_, _, person, _) -> person.id())
                .list(),
                Invoice.class, Client.class, Person.class, Meeting.class);
    }

    @TestTemplate
    public void testLeftJoinAsFourthTable(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                SELECT Invoice.id, Meeting.id FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                LEFT JOIN Meeting ON Client.id=Meeting.id
                ORDER BY Invoice.id, Person.id
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .leftJoin(Meeting.class, (_, client, _, meeting) -> client.id().equals(meeting.id()))
                .orderBy((invoice, _, _, _) -> invoice.id())
                .thenBy((_, _, person, _) -> person.id())
                .list((invoice, _, _, _) -> invoice.id(), (_, _, _, meeting) -> meeting.id()),
                Long.class, Long.class);
    }

    @TestTemplate
    public void testRightJoinAsFourthTable(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                SELECT Meeting.id, Client.id FROM Invoice
                JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                RIGHT JOIN Meeting ON Client.id=Meeting.id
                ORDER BY Meeting.id, Invoice.id, Person.id
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .rightJoin(Meeting.class, (_, client, _, meeting) -> client.id().equals(meeting.id()))
                .orderBy((_, _, _, meeting) -> meeting.id())
                .thenBy((invoice, _, _, _) -> invoice.id())
                .thenBy((_, _, person, _) -> person.id())
                .list((_, _, _, meeting) -> meeting.id(), (_, client, _, _) -> client.id()),
                Long.class, Long.class);
    }
}
