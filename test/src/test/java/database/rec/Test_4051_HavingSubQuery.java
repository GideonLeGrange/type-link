package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static me.legrange.typelink.Selects.count;
import static me.legrange.typelink.Selects.sum;

public final class Test_4051_HavingSubQuery extends DatabaseTest {

    @TestTemplate
    public void testSubQueryWithHaving(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId IN
                (SELECT Invoice.clientId FROM Invoice
                    GROUP BY Invoice.clientId
                    HAVING COUNT(*) > 1)
                """, Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> db.from(Invoice.class)
                        .groupBy(Invoice::clientId)
                        .having(inv -> count(inv) > 1)
                        .list(Invoice::clientId)
                        .contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithHavingSum(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId IN
                (SELECT Invoice.clientId FROM Invoice
                    GROUP BY Invoice.clientId
                    HAVING SUM(Invoice.amount) > 100)
                """, Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> db.from(Invoice.class)
                        .groupBy(Invoice::clientId)
                        .having(inv -> sum(inv.amount()) > 100)
                        .list(Invoice::clientId)
                        .contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithHavingInColumn(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Client.name,
                (SELECT SUM(Invoice.amount) FROM Invoice
                    WHERE Invoice.clientId = Client.id
                    GROUP BY Invoice.clientId
                    HAVING COUNT(*) > 1)
                FROM Client
                """, String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(Client::name, c ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .groupBy(Invoice::clientId)
                                .having(i -> count(i) > 1)
                                .sum(Invoice::amount));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithHavingJoin(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId IN
                (SELECT Client.id FROM Invoice
                    JOIN Client ON Invoice.clientId = Client.id
                    GROUP BY Client.id
                    HAVING SUM(Invoice.amount) > 100)
                """, Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> db.from(Invoice.class)
                        .join(Client.class, (inv, c) -> inv.clientId().equals(c.id()))
                        .groupBy((_, c) -> c.id())
                        .having((inv, _) -> sum(inv.amount()) > 100)
                        .list((_, c) -> c.id())
                        .contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithHavingMultipleConditions(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Client WHERE Client.id IN
                (SELECT Invoice.clientId FROM Invoice
                    GROUP BY Invoice.clientId
                    HAVING COUNT(*) > 1 AND SUM(Invoice.amount) > 50)
                """, Client.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .where(c -> db.from(Invoice.class)
                        .groupBy(Invoice::clientId)
                        .having(i -> count(i) > 1 && sum(i.amount()) > 50)
                        .list(Invoice::clientId)
                        .contains(c.id()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testNestedSubQueryWithHaving(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Client WHERE Client.id IN
                (SELECT Invoice.clientId FROM Invoice
                    WHERE Invoice.amount > (SELECT AVG(Invoice.amount) FROM Invoice)
                    GROUP BY Invoice.clientId
                    HAVING COUNT(*) > 1)
                """, Client.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .where(c -> db.from(Invoice.class)
                        .where(i -> i.amount() > db.from(Invoice.class).avg(Invoice::amount))
                        .groupBy(Invoice::clientId)
                        .having(i -> count(i) > 1)
                        .list(Invoice::clientId)
                        .contains(c.id()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithHavingAndOrderBy(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Client.name,
                (SELECT SUM(Invoice.amount) FROM Invoice
                    WHERE Invoice.clientId = Client.id
                    GROUP BY Invoice.clientId
                    HAVING SUM(Invoice.amount) > 50
                    ORDER BY SUM(Invoice.amount) DESC)
                FROM Client
                """, String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(Client::name, c ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .groupBy(Invoice::clientId)
                                .having(i -> sum(i.amount()) > 50)
                                .orderByDescending(i -> sum(i.amount()))
                                .sum(Invoice::amount));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithHavingThreeTables(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Person.name FROM Person WHERE Person.clientId IN
                (SELECT Client.id FROM Invoice
                    JOIN Client ON Invoice.clientId = Client.id
                    JOIN Person ON Client.id = Person.clientId
                    GROUP BY Client.id
                    HAVING SUM(Invoice.amount) > 100)
                """, String.class);
        var db = db(testDb);
        var have = db.from(Person.class)
                .where(p -> db.from(Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, per) -> c.id().equals(per.clientId()))
                        .groupBy((_, c, _) -> c.id())
                        .having((i, _, _) -> sum(i.amount()) > 100)
                        .list((_, c, _) -> c.id())
                        .contains(p.clientId()))
                .list(Person::name);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testScalarSubQueryWithHavingCount(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Client.name,
                (SELECT COUNT(*) FROM Invoice
                    WHERE Invoice.clientId = Client.id
                    GROUP BY Invoice.clientId
                    HAVING COUNT(*) > 1)
                FROM Client
                """, String.class, Long.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(Client::name, c ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .groupBy(Invoice::clientId)
                                .having(i -> count(i) > 1)
                                .count());
        assertExpected(want, have);
    }
}

