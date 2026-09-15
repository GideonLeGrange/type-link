package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestContainerDatabase;
import database.testing.TestDatabase;
import me.legrange.typelink.Row2;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0700_Join extends DatabaseTest {

    @TestTemplate
    public void testJoin(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Client JOIN Invoice ON Client.id=Invoice.clientId""",
                from(testDb, Client.class)
                        .join(Invoice.class, (client, invoice) -> invoice.clientId().equals(client.id()))
                        .list((client, _) -> client),
                Client.class
        );
    }

    @TestTemplate
    public void testJoinWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Client JOIN Invoice ON Client.id=Invoice.clientId
                        WHERE Client.name LIKE 'Acme%'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .where((_, client) -> client.name().startsWith("Acme"))
                        .list((invoice, _) -> invoice),
                Invoice.class);
    }

    @TestTemplate
    public void testJoinJoin(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                SELECT Invoice.* FROM Invoice JOIN Client ON Invoice.clientId=Client.id
                JOIN Person ON Client.id=Person.clientId
                """, from(testDb, Invoice.class)
                .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                .list((invoice, _, _) -> invoice), Invoice.class
        );
    }

    @TestTemplate
    public void testJoinJoinWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId WHERE Person.name LIKE '%Bob%'
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
    public void testLeftJoin(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Client LEFT JOIN Invoice ON Client.id=Invoice.clientId", from(testDb, Client.class)
                .leftJoin(Invoice.class, (client, invoice) -> invoice.clientId().equals(client.id()))
                .list((client, _) -> client), Client.class);
    }

    @TestTemplate
    public void testRightJoin(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Client RIGHT JOIN Invoice ON Client.id=Invoice.clientId""",
                from(testDb, Client.class)
                        .rightJoin(Invoice.class, (client, invoice) -> invoice.clientId().equals(client.id()))
                        .list((client, _) -> client),
                Client.class
        );
    }

    @TestTemplate
    public void testJoinWithComplexWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Client JOIN Invoice ON Client.id=Invoice.clientId
                        WHERE Client.name LIKE 'Acme%' AND Invoice.amount > 100""",
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .where((invoice, client) -> client.name().startsWith("Acme") && invoice.amount() > 100)
                        .list((invoice, _) -> invoice),
                Invoice.class);
    }

    @TestTemplate
    public void testJoinJoinWithComplexWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice JOIN Client ON Invoice.clientId=Client.id
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
    public void testJoinWithOrWhere(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Client JOIN Invoice ON Client.id=Invoice.clientId
                        WHERE Client.name = 'Acme Corp' OR Invoice.amount > 900""",
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .where((invoice, client) -> client.name().equals("Acme Corp") || invoice.amount() > 900)
                        .list((invoice, _) -> invoice),
                Invoice.class);
    }

    @TestTemplate
    public void testLFullJoin(TestDatabase testDb) throws SQLException {
        if (testDb.equals(TestContainerDatabase.POSTGRES)) {
            testListOfRecord(testDb, "SELECT Client.* FROM Client FULL OUTER JOIN Invoice ON Invoice.clientId = Client.id", from(testDb, Client.class)
                    .fullJoin(Invoice.class, (client, invoice) -> invoice.clientId().equals(client.id()))
                    .list((client, _) -> client), Client.class);
        }
    }

    @TestTemplate
    public void testJoinWithResultsFromTwoTables(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT Invoice.id, Client.id FROM Client JOIN Invoice ON Client.id=Invoice.clientId
                        WHERE Client.name = 'Acme Corp'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .where((_, client) -> client.name().equals("Acme Corp"))
                        .list((invoice, _) -> invoice.id(), (_, client) -> client.id()), Long.class, Long.class);
    }

    @TestTemplate
    public void testJoinToRows(TestDatabase testDb) throws SQLException {
        var have = from(testDb, Client.class)
                .join(Invoice.class, (client, invoice) -> invoice.clientId().equals(client.id()))
                .list();
        var want = select(testDb, """
                        SELECT Client.*,Invoice.* FROM Client JOIN Invoice ON Client.id=Invoice.clientId""",
                Client.class, Invoice.class);
        assertExpected(want, have);
        assertThat(have.getFirst())
                .isInstanceOf(Row2.class);
        assertThat(have.getFirst().v1())
                .isInstanceOf(Client.class);
        assertThat(have.getFirst().v2())
                .isInstanceOf(Invoice.class);
    }

}
