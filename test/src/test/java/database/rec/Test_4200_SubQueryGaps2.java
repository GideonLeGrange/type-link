package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Additional sub-query tests covering gaps:
 * 7. Sub-query with multiple FROM tables in the outer query
 * 8. Sub-query returning no rows (empty/NULL result)
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_4200_SubQueryGaps2 extends DatabaseTest {

    // --- Gap 7: Multi-table outer query with sub-queries ---

    @TestTemplate
    public void testFrom2WhereWithScalarSubQuery(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Invoice.amount, Client.name FROM Invoice, Client
                WHERE Invoice.clientId = Client.id
                AND Invoice.amount > (SELECT AVG(Invoice.amount) FROM Invoice)""",
                Double.class, String.class);
        var db = db(testDb);
        var have = db.from(Invoice.class, Client.class)
                .where((i, c) -> i.clientId().equals(c.id())
                        && i.amount() > db.from(Invoice.class).avg(Invoice::amount))
                .list((i, _) -> i.amount(), (_, c) -> c.name());
        assertExpected(want, have);
    }

    @TestTemplate
    public void testFrom2WhereWithInSubQuery(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Invoice.amount, Client.name FROM Invoice, Client
                WHERE Invoice.clientId = Client.id
                AND Client.id IN
                (SELECT Person.clientId FROM Person WHERE Person.name LIKE 'Bob%')""",
                Double.class, String.class);
        var db = db(testDb);
        var have = db.from(Invoice.class, Client.class)
                .where((i, c) -> i.clientId().equals(c.id())
                        && db.from(Person.class)
                                .where(p -> p.name().startsWith("Bob"))
                                .list(Person::clientId).contains(c.id()))
                .list((i, _) -> i.amount(), (_, c) -> c.name());
        assertExpected(want, have);
    }

    @TestTemplate
    public void testFrom2WithSubQueryInColumns(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Client.name,
                (SELECT MIN(Invoice.amount) FROM Invoice WHERE Invoice.clientId = Client.id)
                FROM Invoice, Client
                WHERE Invoice.clientId = Client.id""",
                String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Invoice.class, Client.class)
                .where((i, c) -> i.clientId().equals(c.id()))
                .list((_, c) -> c.name(),
                        (_, c) -> db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .min(Invoice::amount));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testFrom2WithNotInSubQuery(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Invoice.amount, Client.name FROM Invoice, Client
                WHERE Invoice.clientId = Client.id
                AND Client.id NOT IN
                (SELECT Person.clientId FROM Person WHERE Person.name LIKE 'Rhode%')""",
                Double.class, String.class);
        var db = db(testDb);
        var have = db.from(Invoice.class, Client.class)
                .where((i, c) -> i.clientId().equals(c.id())
                        && !db.from(Person.class)
                                .where(p -> p.name().startsWith("Rhode"))
                                .list(Person::clientId).contains(c.id()))
                .list((i, _) -> i.amount(), (_, c) -> c.name());
        assertExpected(want, have);
    }

    // --- Gap 8: Sub-query returning no rows / NULL ---

    @TestTemplate
    public void testSubQueryReturningNoRowsInWhere(TestDatabase testDb) throws SQLException {
        // Sub-query IN with no matching rows should return empty outer result
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId IN
                (SELECT Client.id FROM Client WHERE Client.name = 'NonExistentClient12345')""",
                Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i ->
                        db.from(Client.class)
                                .where(c -> c.name().equals("NonExistentClient12345"))
                                .list(Client::id).contains(i.clientId()))
                .list();
        assertThat(have)
                .isNotNull()
                .isEmpty();
        assertThat(want)
                .isNotNull()
                .isEmpty();
    }

    @TestTemplate
    public void testSubQueryReturningNullScalarInColumn(TestDatabase testDb) throws SQLException {
        // Correlated sub-query that returns NULL for clients with no invoices
        // Use Vendor table which has no invoices pointing to it
        var want = select(testDb, """
                SELECT Client.name,
                (SELECT SUM(Invoice.amount) FROM Invoice WHERE Invoice.clientId = -1)
                FROM Client""",
                String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(Client::name, _ ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(-1L))
                                .sum(Invoice::amount));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testNotInSubQueryReturningNoRows(TestDatabase testDb) throws SQLException {
        // NOT IN with empty sub-query should return all outer rows
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId NOT IN
                (SELECT Client.id FROM Client WHERE Client.name = 'NonExistentClient12345')""",
                Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i ->
                        !db.from(Client.class)
                                .where(c -> c.name().equals("NonExistentClient12345"))
                                .list(Client::id).contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testScalarSubQueryNoMatchInWhere(TestDatabase testDb) throws SQLException {
        // Comparing against a sub-query that returns NULL (no rows match) should yield no results
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount >
                (SELECT AVG(Invoice.amount) FROM Invoice WHERE Invoice.clientId = -1)""",
                Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() >
                        db.from(Invoice.class)
                                .where(inv -> inv.clientId().equals(-1L))
                                .avg(Invoice::amount))
                .list();
        // Both should be empty since comparing against NULL yields no matches
        assertThat(have)
                .isNotNull()
                .isEqualTo(want);
    }
}

