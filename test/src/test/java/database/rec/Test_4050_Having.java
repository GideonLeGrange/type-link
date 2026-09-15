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

public final class Test_4050_Having extends DatabaseTest {

    @TestTemplate
    public void testCountHaving(TestDatabase testDb) throws SQLException {
        var have = db(testDb)
                .from(Invoice.class)
                .groupBy(Invoice::clientId)
                .having(i -> count(i) > 1)
                .list(Invoice::clientId, count());
        var want = select(testDb, """
                        SELECT Invoice.clientId, COUNT(*) FROM Invoice
                        GROUP BY clientId HAVING COUNT(*) > 1
                        """, Long.class, Long.class);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testCountHavingTwoTables(TestDatabase testDb) throws SQLException {
        var have = db(testDb)
                .from(Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .groupBy((_, c) -> c.name())
                .having((i, _) -> sum(i.amount()) > 100)
                .list((_, c) -> c.name(), (i, _) -> sum(i.amount()));
        var want = select(testDb, """
                        SELECT Client.name, SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        GROUP BY Client.name
                        HAVING SUM(Invoice.amount) > 100
                        """, String.class, Double.class);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testCountHavingThreeTables(TestDatabase testDb) throws SQLException {
        var have = db(testDb)
                .from(Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                .groupBy((_, _, p) -> p.email())
                .having((i, _, _) -> sum(i.amount()) > 100)
                .list((_, _, p) -> p.email(), (i, _, _) -> sum(i.amount()));
        var want = select(testDb, """
                        SELECT Person.email, SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        GROUP BY Person.email
                        HAVING SUM(Invoice.amount) > 100
                        """, String.class, Double.class);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testHavingWithOrderBy(TestDatabase testDb) throws SQLException {
        var have = db(testDb)
                .from(Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .groupBy((_, c) -> c.name())
                .having((i, _) -> count(i) > 1)
                .orderBy((i, _) -> sum(i.amount()))
                .list((_, c) -> c.name(), (i, _) -> sum(i.amount()));
        var want = select(testDb, """
                        SELECT Client.name, SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        GROUP BY Client.name
                        HAVING COUNT(*) > 1
                        ORDER BY SUM(Invoice.amount)
                        """, String.class, Double.class);
        assertExpected(want, have);
    }
}
