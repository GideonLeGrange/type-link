package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static rec.Person.Sex.FEMALE;
import static rec.Person.Sex.MALE;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0711_Join_AndOr extends DatabaseTest {

    // ========== Two Table Join Tests (From2) ==========

    @TestTemplate
    public void testJoin_Where_And(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Person JOIN Client ON Person.clientId=Client.id
                        WHERE Person.sex='MALE' AND Person.age>18""",
                from(testDb, Person.class)
                        .join(Client.class, (person, client) -> person.clientId().equals(client.id()))
                        .where((person, _) -> person.sex() == MALE)
                        .and((person, _) -> person.age() > 18)
                        .list((person, _) -> person), Person.class
        );
    }

    @TestTemplate
    public void testJoin_Where_And_And(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Person JOIN Client ON Person.clientId=Client.id
                        WHERE Person.sex='MALE' AND Person.age>18 AND Person.age<70""",
                from(testDb, Person.class)
                        .join(Client.class, (person, client) -> person.clientId().equals(client.id()))
                        .where((person, _) -> person.sex() == MALE)
                        .and((person, _) -> person.age() > 18)
                        .and((person, _) -> person.age() < 70)
                        .list((person, _) -> person), Person.class
        );
    }

    @TestTemplate
    public void testJoin_Where_Or(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Person JOIN Client ON Person.clientId=Client.id
                        WHERE Person.sex='MALE' OR Person.age<20""",
                from(testDb, Person.class)
                        .join(Client.class, (person, client) -> person.clientId().equals(client.id()))
                        .where((person, _) -> person.sex() == MALE)
                        .or((person, _) -> person.age() < 20)
                        .list((person, _) -> person), Person.class
        );
    }

    @TestTemplate
    public void testJoin_Where_Or_Or(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Person JOIN Client ON Person.clientId=Client.id
                        WHERE Person.sex='MALE' OR Person.age<20 OR Client.name='Acme Inc.'""",
                from(testDb, Person.class)
                        .join(Client.class, (person, client) -> person.clientId().equals(client.id()))
                        .where((person, _) -> person.sex() == MALE)
                        .or((person, _) -> person.age() < 20)
                        .or((_, client) -> client.name().equals("Acme Inc."))
                        .list((person, _) -> person), Person.class
        );
    }

    // Mixed And/Or chains removed - use lambda expressions for complex precedence

    @TestTemplate
    public void testJoin_And_WithBothTables(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Person JOIN Client ON Person.clientId=Client.id
                WHERE Person.sex='FEMALE' AND Client.name='Acme Corp'""", Person.class);
        var have =

                from(testDb, Person.class)
                        .join(Client.class, (person, client) -> person.clientId().equals(client.id()))
                        .where((person, _) -> person.sex() == FEMALE)
                        .and((_, client) -> client.name().equals("Acme Corp"))
                        .list((person, _) -> person);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testJoin_Or_WithBothTables(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Person JOIN Client ON Person.clientId=Client.id
                        WHERE Person.age<20 OR Client.name='Acme Inc.'""",
                from(testDb, Person.class)
                        .join(Client.class, (person, client) -> person.clientId().equals(client.id()))
                        .where((person, _) -> person.age() < 20)
                        .or((_, client) -> client.name().equals("Acme Inc."))
                        .list((person, _) -> person), Person.class
        );
    }

    @TestTemplate
    public void testJoin_Where_And_WithOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Person JOIN Client ON Person.clientId=Client.id
                        WHERE Person.sex='MALE' AND Person.age>18
                        ORDER BY Person.age""",
                from(testDb, Person.class)
                        .join(Client.class, (person, client) -> person.clientId().equals(client.id()))
                        .where((person, _) -> person.sex() == MALE)
                        .and((person, _) -> person.age() > 18)
                        .orderBy((person, _) -> person.age())
                        .list((person, _) -> person), Person.class
        );
    }

    @TestTemplate
    public void testJoin_Where_Or_WithLimit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Person JOIN Client ON Person.clientId=Client.id
                        WHERE Person.sex='MALE' OR Person.age<20
                        LIMIT 5""",
                from(testDb, Person.class)
                        .join(Client.class, (person, client) -> person.clientId().equals(client.id()))
                        .where((person, _) -> person.sex() == MALE)
                        .or((person, _) -> person.age() < 20)
                        .limit(5)
                        .list((person, _) -> person), Person.class
        );
    }

    // ========== Three Table Join Tests (From3) ==========

    @TestTemplate
    public void testJoin2_Where_And(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Person.sex='MALE' AND Person.age>18""",
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                        .where((_, _, person) -> person.sex() == MALE)
                        .and((_, _, person) -> person.age() > 18)
                        .list((invoice, _, _) -> invoice), Invoice.class
        );
    }

    @TestTemplate
    public void testJoin2_Where_Or(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Person.sex='MALE' OR Person.age<20""",
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                        .where((_, _, person) -> person.sex() == MALE)
                        .or((_, _, person) -> person.age() < 20)
                        .list((invoice, _, _) -> invoice), Invoice.class
        );
    }

    @TestTemplate
    public void testJoin2_Where_And_And(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Person.sex='MALE' AND Person.age>18 AND Client.name='Acme Corp'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                        .where((_, _, person) -> person.sex() == MALE)
                        .and((_, _, person) -> person.age() > 18)
                        .and((_, client, _) -> client.name().equals("Acme Corp"))
                        .list((invoice, _, _) -> invoice), Invoice.class
        );
    }

    @TestTemplate
    public void testJoin2_Where_Or_Or(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Person.sex='MALE' OR Person.age<20 OR Invoice.amount>1000""",
                from(testDb, Invoice.class)
                        .join(Client.class, (invoice, client) -> invoice.clientId().equals(client.id()))
                        .join(Person.class, (_, client, person) -> client.id().equals(person.clientId()))
                        .where((_, _, person) -> person.sex() == MALE)
                        .or((_, _, person) -> person.age() < 20)
                        .or((invoice, _, _) -> invoice.amount() > 1000)
                        .list((invoice, _, _) -> invoice), Invoice.class
        );
    }

    // Mixed And/Or chains removed - use lambda expressions for complex precedence
}

