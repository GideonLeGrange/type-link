package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Client;
import rec.Invoice;
import rec.Person;
import me.legrange.typelink.Selects;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.util.List;

import static me.legrange.typelink.Selects.avg;
import static me.legrange.typelink.Selects.count;
import static me.legrange.typelink.Selects.max;
import static me.legrange.typelink.Selects.min;
import static me.legrange.typelink.Selects.sum;

public final class Test_1100_Aggregate extends DatabaseTest {

    @TestTemplate
    public void testAggregateSum(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(amount) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> sum(i.amount())), Double.class);
    }

    @TestTemplate
    public void testAggregateMax(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(amount) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> max(i.amount())), Double.class);
    }

    @TestTemplate
    public void testAggregateMin(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(amount) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> min(i.amount())), Double.class);
    }

    @TestTemplate
    public void testAggregateAvg(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT AVG(amount) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> avg(i.amount())), Double.class);
    }

    @TestTemplate
    public void testAggregateCount(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT COUNT(email) FROM Person",
                from(testDb, Person.class).aggregate(p -> count(p.email())), Long.class);
    }

    @TestTemplate
    public void testAggregateCountRows(TestDatabase testDb) throws SQLException {
        //noinspection Convert2MethodRef
        testNumber(testDb, "SELECT COUNT(*) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> count(i)), Long.class);
    }

    @TestTemplate
    public void testAggregateCountRowsRef(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT COUNT(*) FROM Invoice",
                from(testDb, Invoice.class).aggregate(Selects::count), Long.class);
    }

    @TestTemplate
    public void testAggregateSumAndCount(TestDatabase testDb) throws SQLException {
        testRow(testDb, "SELECT SUM(amount),COUNT(*) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> sum(i.amount()), count()));
    }

    @TestTemplate
    public void testAggregateSumWithMath(TestDatabase testDb) throws SQLException {
        testRow(testDb, "SELECT SUM(amount)/2,COUNT(*) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> sum(i.amount() / 2), count()));
    }

    @TestTemplate
    public void testAggregateSumAndAverage(TestDatabase testDb) throws SQLException {
        testRow(testDb, "SELECT SUM(amount),AVG(amount) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> sum(i.amount()), i -> avg(i.amount())));
    }

    @TestTemplate
    public void testAggregateMinAndMax(TestDatabase testDb) throws SQLException {
        testRow(testDb, "SELECT MIN(amount),MAX(amount) FROM Invoice",
                from(testDb, Invoice.class).aggregate(i -> min(i.amount()), i -> max(i.amount())));
    }

    @TestTemplate
    public void testAggregateMinAndMaxAndAvg(TestDatabase testDb) throws SQLException {
        testRow(testDb, "SELECT MIN(amount),MAX(amount),AVG(amount) FROM Invoice",
                from(testDb, Invoice.class)
                        .aggregate(i -> min(i.amount()),
                                i -> max(i.amount()),
                                i -> avg(i.amount())));
    }

    @TestTemplate
    public void testAggregateOnJoinCount(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT Client.name,COUNT(*) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        WHERE Client.name LIKE 'Acme%'
                        GROUP BY Client.name""", List.of(String.class, Long.class));
                var have  =from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().startsWith("Acme"))
                        .groupBy((_, c) -> c.name())
                        .aggregate((_, c) -> c.name(), (_, _) -> count());
                assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinMax(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT Client.name,MAX(amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        WHERE Client.name LIKE 'Acme%'
                        GROUP BY Client.name""", List.of(String.class, Double.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .where((_, c) -> c.name().startsWith("Acme"))
                .groupBy((_, c) -> c.name())
                .aggregate((_, c) -> c.name(), (i, _) -> max(i.amount()));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinJoinTwoValues(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT SUM(Invoice.amount), COUNT(*) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Person.name LIKE 'John%'""", List.of(Double.class, Long.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                .where((_, _, p) -> p.name().startsWith("John"))
                .aggregate((i, _, _) -> sum(i.amount()), (_, _, _) -> count());
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinJoinThreeValues(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT MIN(Invoice.amount), MAX(Invoice.amount), AVG(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Person.age > 18""", List.of(Double.class, Double.class, Double.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                .where((_, _, p) -> p.age() > 18)
                .aggregate((i, _, _) -> min(i.amount()),
                        (i, _, _) -> max(i.amount()),
                        (i, _, _) -> avg(i.amount()));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinJoinWithGroupByTwoValues(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT Person.name, SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Person.name = 'Bob Jones'
                        GROUP BY Person.name""", List.of(String.class, Double.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                .where((_, _, p) -> p.name().equals("Bob Jones"))
                .groupBy((_, _, p) -> p.name())
                .aggregate((_, _, p) -> p.name(), (i, _, _) -> sum(i.amount()));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinJoinWithGroupByThreeValues(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT Client.name, SUM(Invoice.amount), COUNT(*) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Client.name LIKE 'Acme%'
                        GROUP BY Client.name""", List.of(String.class, Double.class, Long.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                .where((_, c, _) -> c.name().startsWith("Acme"))
                .groupBy((_, c, _) -> c.name())
                .aggregate((_, c, _) -> c.name(),
                        (i, _, _) -> sum(i.amount()),
                        (_, _, _) -> count());
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinJoinSumAndAvg(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT SUM(Invoice.amount), AVG(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId""", List.of(Double.class, Double.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                .aggregate((i, _, _) -> sum(i.amount()), (i, _, _) -> avg(i.amount()));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinJoinMinMaxCount(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT MIN(Person.age), MAX(Person.age), COUNT(*) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        JOIN Person ON Client.id=Person.clientId
                        WHERE Invoice.paid = true""", List.of(Integer.class, Integer.class, Long.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                .where((i, _, _) -> i.paid())
                .aggregate((_, _, p) -> min(p.age()),
                        (_, _, p) -> max(p.age()),
                        (_, _, _) -> count());
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinThreeValues(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT MIN(Invoice.amount), MAX(Invoice.amount), AVG(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        WHERE Client.name LIKE 'Acme%'""", List.of(Double.class, Double.class, Double.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .where((_, c) -> c.name().startsWith("Acme"))
                .aggregate((i, _) -> min(i.amount()),
                        (i, _) -> max(i.amount()),
                        (i, _) -> avg(i.amount()));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinWithGroupByThreeValues(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT Client.name, MIN(Invoice.amount), MAX(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id
                        WHERE Invoice.paid = true
                        GROUP BY Client.name
                        LIMIT 1""", List.of(String.class, Double.class, Double.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .where((i, _) -> i.paid())
                .groupBy((_, c) -> c.name())
                .limit(1)
                .aggregate((_, c) -> c.name(),
                        (i, _) -> min(i.amount()),
                        (i, _) -> max(i.amount()));
        assertExpected(want, have);
    }

    @TestTemplate
    public void testAggregateOnJoinSumAvgCount(TestDatabase testDb) throws SQLException {
        var want = selectRow(testDb, """
                        SELECT SUM(Invoice.amount), AVG(Invoice.amount), COUNT(*) FROM Invoice
                        JOIN Client ON Invoice.clientId=Client.id""", List.of(Double.class, Double.class, Long.class));
        var have = from(testDb, Invoice.class)
                .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                .aggregate((i, _) -> sum(i.amount()),
                        (i, _) -> avg(i.amount()),
                        (_, _) -> count());
        assertExpected(want, have);
    }

    
}
