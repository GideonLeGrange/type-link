package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_4000_SubQuery extends DatabaseTest {

    @TestTemplate
    public void testScalarSubQueryAvg(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount >
                (SELECT AVG(Invoice.amount) FROM Invoice)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() >
                        db.from(Invoice.class).avg(Invoice::amount))
                .list();
        assertExpected(want, have);
    }

    @SuppressWarnings("Convert2MethodRef")
    @TestTemplate
    public void testScalarSubQueryWhere(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Client WHERE Client.id IN
                (SELECT Client.id FROM Client WHERE name LIKE 'Acme%' ORDER BY name DESC)""", Client.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .where(c ->
                        db.from(Client.class)
                                .where(client -> client.name().startsWith("Acme"))
                                .orderByDescending(client -> client.name())
                                .list(client -> client.id()).contains(c.id()))
                .list();
        assertExpected(want, have);
    }


    @TestTemplate
    public void testScalarSubQueryMax(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Invoice.* FROM Invoice WHERE Invoice.amount =
                (SELECT MAX(Invoice.amount) FROM Invoice)""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() ==
                        db.from(Invoice.class).max(Invoice::amount))
                .list();
        assertExpected(want, have);
    }

    @SuppressWarnings("Convert2MethodRef")
    @TestTemplate
    public void testColumnSubQuery(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Invoice.* FROM Invoice WHERE Invoice.clientId IN
                (SELECT Client.id FROM Client WHERE Client.name LIKE 'Acme%')""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i ->
                        db.from(Client.class)
                                .where(c -> c.name().startsWith("Acme"))
                                .list(c -> c.id()).contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    @TestTemplate
    public void testScalarSubQuerySumGroupBy(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT name,
                (SELECT SUM(amount) FROM Invoice WHERE Invoice.clientId = Client.id GROUP BY Invoice.clientId)
                FROM Client""", String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(Client::name, c ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .groupBy(Invoice::clientId)
                                .sum(Invoice::amount));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryInColumns(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT (SELECT name FROM Client WHERE Invoice.clientId=Client.id), amount FROM Invoice""", String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .list(i ->
                        db.from(Client.class)
                                .where(c -> i.clientId().equals(c.id()))
                                .list(Client::name), Invoice::amount);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryInColumnsWithOrderBy(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT
                (SELECT name FROM Client WHERE Invoice.clientId=Client.id ORDER BY Client.name),
                amount FROM Invoice""", String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .list(i ->
                        db.from(Client.class)
                                .where(c -> i.clientId().equals(c.id()))
                                .orderBy(Client::name)
                                .list(Client::name), Invoice::amount);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryInColumnsWithLimit(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT
                (SELECT name FROM Client WHERE Invoice.clientId=Client.id AND name like 'Acme%' LIMIT 1),
                amount FROM Invoice""", String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .list(i ->
                        db.from(Client.class)
                                .where(c -> i.clientId().equals(c.id())
                                        && c.name().startsWith("Acme"))
                                .limit(1)
                                .list(Client::name), Invoice::amount);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithJoin(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT amount
                FROM Invoice WHERE Invoice.clientId IN
                (SELECT Client.id FROM Client
                    JOIN Person ON Client.id = Person.clientId
                    WHERE Person.name LIKE 'Rhode%')
                """, Double.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> db.from(Client.class)
                        .join(Person.class, (c, p) -> c.id().equals(p.clientId()))
                        .where((_, p) -> p.name().startsWith("Rhode"))
                        .list((c, _) -> c.id())
                        .contains(i.clientId()))
                .list(Invoice::amount);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQuerySubQuery(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.clientId IN
                (SELECT Client.id FROM Client WHERE Client.id IN
                    (SELECT Person.clientId FROM Person WHERE Person.name LIKE 'Bob%'))
                """, Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> db.from(Client.class)
                        .where(c -> db.from(Person.class)
                                .where(p -> p.name().startsWith("Bob"))
                                .list(Person::clientId)
                                .contains(c.id()))
                        .list(Client::id)
                        .contains(i.clientId()))
                .list();
        assertExpected(want, have);
    }

    @SuppressWarnings("Convert2MethodRef")
    @TestTemplate
    public void testSubQueryCount(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Client.name, (SELECT COUNT(*) FROM Invoice
                WHERE Invoice.clientId=Client.id GROUP BY Invoice.clientId)
                FROM Client
                """, String.class, Long.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(c -> c.name(), c ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .groupBy(i -> i.clientId())
                                .count());
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryCounRef(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Client.name, (SELECT COUNT(*) FROM Invoice
                WHERE Invoice.clientId=Client.id GROUP BY Invoice.clientId)
                FROM Client
                """, String.class, Long.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(Client::name, c ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .groupBy(Invoice::clientId)
                                .count());
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryCountFieldRef(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT Client.name, (SELECT COUNT(*) FROM Invoice
                WHERE Invoice.clientId=Client.id GROUP BY Invoice.clientId)
                FROM Client
                """, String.class, Long.class);
        var db = db(testDb);
        var have = db.from(Client.class)
                .list(Client::name, c ->
                        db.from(Invoice.class)
                                .where(i -> i.clientId().equals(c.id()))
                                .groupBy(Invoice::clientId)
                                .count(Invoice::id));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithAnd(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT
                (SELECT name FROM Client WHERE Invoice.clientId=Client.id AND name like 'Acme%' LIMIT 1),
                amount FROM Invoice""", String.class, Double.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .list(i ->
                        db.from(Client.class)
                                .where(c -> i.clientId().equals(c.id()))
                                .and(c -> c.name().startsWith("Acme"))
                                .limit(1)
                                .list(Client::name), Invoice::amount);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testSubQueryWithJoinAndOr(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT amount
                FROM Invoice WHERE Invoice.clientId IN
                (SELECT Client.id FROM Client
                    JOIN Person ON Client.id = Person.clientId
                    WHERE Person.name LIKE 'Rhode%' OR Person.name LIKE 'Bob%')
                """, Double.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> db.from(Client.class)
                        .join(Person.class, (c, p) -> c.id().equals(p.clientId()))
                        .where((_, p) -> p.name().startsWith("Rhode"))
                        .or((_,p) -> p.name().startsWith("Bob"))
                        .list((c, _) -> c.id())
                        .contains(i.clientId()))
                .list(Invoice::amount);
        assertExpected(want, have);
    }

}
