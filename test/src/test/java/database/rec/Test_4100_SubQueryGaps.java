package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

/**
 * Additional sub-query tests covering gaps:
 * 1. NOT IN with sub-queries
 * 4. Sub-query with MIN
 * 5. Sub-query with LIMIT in WHERE
 * 6. Sub-query with OR in outer query
 * 9. Sub-query with <, <=, >=, != comparison operators
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_4100_SubQueryGaps extends DatabaseTest {

    // --- Gap 1: NOT IN with sub-queries ---

    @TestTemplate
    public void testNotInSubQuery(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId NOT IN
                (SELECT Client.id FROM Client WHERE Client.name LIKE 'Acme%')""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i ->
                        !db.from(Client.class)
                                .where(c -> c.name().startsWith("Acme"))
                                .list(Client::id).contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testNotInSubQueryWithJoin(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId NOT IN
                (SELECT Client.id FROM Client
                    JOIN Person ON Client.id = Person.clientId
                    WHERE Person.name LIKE 'Rhode%')""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i ->
                        !db.from(Client.class)
                                .join(Person.class, (c, p) -> c.id().equals(p.clientId()))
                                .where((_, p) -> p.name().startsWith("Rhode"))
                                .list((c, _) -> c.id()).contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    // --- Gap 4: Sub-query with MIN ---

    @TestTemplate
    public void testScalarSubQueryMin(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount =
                (SELECT MIN(Invoice.amount) FROM Invoice)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() ==
                        db.from(Invoice.class).min(Invoice::amount))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryMinInColumn(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Client.name,
                (SELECT MIN(Invoice.amount) FROM Invoice WHERE Invoice.clientId = Client.id)
                FROM Client""", String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(Client::name, c ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .min(Invoice::amount));
        assertExpected(want, have);
    }

    // --- Gap 5: Sub-query with LIMIT in WHERE ---

    @TestTemplate
    public void testSubQueryWithLimitInWhere(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId IN
                (SELECT Client.id FROM Client WHERE Client.name LIKE 'Acme%' LIMIT 1)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i ->
                        db.from(Client.class)
                                .where(c -> c.name().startsWith("Acme"))
                                .limit(1)
                                .list(Client::id).contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    // --- Gap 6: Sub-query with OR in outer query ---

    @TestTemplate
    public void testOuterOrWithSubQueries(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount >
                (SELECT AVG(Invoice.amount) FROM Invoice)
                OR Invoice.clientId IN
                (SELECT Client.id FROM Client WHERE Client.name LIKE 'Acme%')""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() >
                        db.from(Invoice.class).avg(Invoice::amount))
                .or(i ->
                        db.from(Client.class)
                                .where(c -> c.name().startsWith("Acme"))
                                .list(Client::id).contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testOuterOrSubQueryAndLiteral(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.paid = true
                OR Invoice.amount > (SELECT AVG(Invoice.amount) FROM Invoice)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(Invoice::paid)
                .or(i -> i.amount() > db.from(Invoice.class).avg(Invoice::amount))
                .list();
        assertExpected(want, have);
    }

    // --- Gap 9: Sub-query with <, <=, >=, != comparison operators ---

    @TestTemplate
    public void testSubQueryLessThan(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount <
                (SELECT AVG(Invoice.amount) FROM Invoice)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() <
                        db.from(Invoice.class).avg(Invoice::amount))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryLessThanOrEqual(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount <=
                (SELECT MIN(Invoice.amount) FROM Invoice)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() <=
                        db.from(Invoice.class).min(Invoice::amount))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryGreaterThanOrEqual(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount >=
                (SELECT MAX(Invoice.amount) FROM Invoice)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() >=
                        db.from(Invoice.class).max(Invoice::amount))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryNotEqual(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount <>
                (SELECT MAX(Invoice.amount) FROM Invoice)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() !=
                        db.from(Invoice.class).max(Invoice::amount))
                .list();
        assertExpected(want, have);
    }
}


