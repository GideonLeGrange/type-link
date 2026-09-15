package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0195_Distinct extends DatabaseTest {

    @TestTemplate
    public void testDistinctSingleColumn(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT DISTINCT clientId FROM Person ORDER BY clientId", Long.class);
        var have = from(testDb, Person.class)
                .distinct()
                .orderBy(Person::clientId)
                .list(Person::clientId);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testDistinctCollapsesDuplicateValues(TestDatabase testDb) throws SQLException {
        // "Bob Jones" appears twice in the seed data (ids 1 and 5) - DISTINCT must collapse it to one row.
        var want = select(testDb, "SELECT DISTINCT name FROM Person ORDER BY name", String.class);
        var have = from(testDb, Person.class)
                .distinct()
                .orderBy(Person::name)
                .list(Person::name);
        assertExpected(want, have);
        assertThat(have).doesNotHaveDuplicates();
    }

    @TestTemplate
    public void testDistinctWithWhere(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT DISTINCT clientId FROM Person WHERE age > 18 ORDER BY clientId", Long.class);
        var have = from(testDb, Person.class)
                .where(p -> p.age() > 18)
                .distinct()
                .orderBy(Person::clientId)
                .list(Person::clientId);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testDistinctWithLimit(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT DISTINCT clientId FROM Person ORDER BY clientId LIMIT 2", Long.class);
        var have = from(testDb, Person.class)
                .distinct()
                .orderBy(Person::clientId)
                .limit(2)
                .list(Person::clientId);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testDistinctFullRow(TestDatabase testDb) throws SQLException {
        // Every row has a unique id, so DISTINCT here must not change the row count - it just has
        // to be valid SQL and match the control query exactly.
        testListOfRecord(testDb, "SELECT DISTINCT * FROM Person", from(testDb, Person.class)
                .distinct()
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testDistinctOnJoinCollapsesFanOut(TestDatabase testDb) throws SQLException {
        // Every client with an invoice has several (1 has 4, 2 has 3, 3 has 5, 4 has 1) - the join
        // fans each client out to one row per invoice, so without DISTINCT this would return 12 rows.
        var want = select(testDb, """
                SELECT DISTINCT Client.name FROM Client JOIN Invoice ON Client.id=Invoice.clientId
                ORDER BY Client.name""", String.class);
        var have = from(testDb, Client.class)
                .join(Invoice.class, (client, invoice) -> invoice.clientId().equals(client.id()))
                .distinct()
                .orderBy((client, _) -> client.name())
                .list((client, _) -> client.name());
        assertExpected(want, have);
        assertThat(have).doesNotHaveDuplicates();
    }

    @TestTemplate
    public void testDistinctInSubQuery(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId IN
                (SELECT DISTINCT Person.clientId FROM Person)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i ->
                        db.from(Person.class)
                                .distinct()
                                .list(Person::clientId).contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }
}
