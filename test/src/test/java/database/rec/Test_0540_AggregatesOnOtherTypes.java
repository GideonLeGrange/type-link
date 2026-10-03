package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Client;
import rec.Invoice;
import rec.Person;
import rec.Reading;
import rec.Town;

import java.sql.SQLException;

import static me.legrange.typelink.Selects.avg;
import static me.legrange.typelink.Selects.max;
import static me.legrange.typelink.Selects.min;
import static me.legrange.typelink.Selects.sum;

/**
 * Aggregates over column types other than double, which the other aggregate tests concentrate on: primitive and boxed
 * long, float, short, byte and int.
 */
@SuppressWarnings({"NewClassNamingConvention", "Convert2MethodRef"})
public final class Test_0540_AggregatesOnOtherTypes extends DatabaseTest {

    /**
     * Selects.avg is declared to return its argument's type, but an average is always a Double. The result is taken
     * as a Number so that the value, not the declared type, is what is checked.
     */
    private void testAvg(TestDatabase testDb, String sql, Number have) throws SQLException {
        testNumber(testDb, sql, have.doubleValue(), Double.class);
    }

    // --- primitive long

    @TestTemplate
    public void testSumLong(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(bigNum) FROM Reading", from(testDb, Reading.class)
                .sum(Reading::bigNum), Long.class);
    }

    @TestTemplate
    public void testMinLong(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(bigNum) FROM Reading", from(testDb, Reading.class)
                .min(Reading::bigNum), Long.class);
    }

    @TestTemplate
    public void testMaxLong(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(bigNum) FROM Reading", from(testDb, Reading.class)
                .max(Reading::bigNum), Long.class);
    }

    @TestTemplate
    public void testAvgLong(TestDatabase testDb) throws SQLException {
        testAvg(testDb, "SELECT AVG(bigNum) FROM Reading", from(testDb, Reading.class)
                .aggregate(r -> avg(r.bigNum())));
    }

    @TestTemplate
    public void testSumLongWithMultiply(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(bigNum * 2) FROM Reading", from(testDb, Reading.class)
                .sum(r -> r.bigNum() * 2), Long.class);
    }

    @TestTemplate
    public void testMaxLongWithAdd(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(bigNum + id) FROM Reading", from(testDb, Reading.class)
                .max(r -> r.bigNum() + r.id()), Long.class);
    }

    @TestTemplate
    public void testMinLongWithSubtract(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(bigNum - 1000) FROM Reading", from(testDb, Reading.class)
                .min(r -> r.bigNum() - 1000), Long.class);
    }

    @TestTemplate
    public void testSumLongWithWhere(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(bigNum) FROM Reading WHERE bigNum > 0", from(testDb, Reading.class)
                .where(r -> r.bigNum() > 0)
                .sum(Reading::bigNum), Long.class);
    }

    // --- boxed long

    @TestTemplate
    public void testSumBoxedLong(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(clientId) FROM Invoice", from(testDb, Invoice.class)
                .sum(Invoice::clientId), Long.class);
    }

    @TestTemplate
    public void testMinBoxedLong(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(clientId) FROM Invoice", from(testDb, Invoice.class)
                .min(Invoice::clientId), Long.class);
    }

    @TestTemplate
    public void testMaxBoxedLong(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(id) FROM Invoice", from(testDb, Invoice.class)
                .max(Invoice::id), Long.class);
    }

    @TestTemplate
    public void testAvgBoxedLong(TestDatabase testDb) throws SQLException {
        testAvg(testDb, "SELECT AVG(clientId) FROM Invoice", from(testDb, Invoice.class)
                .aggregate(i -> avg(i.clientId())));
    }

    @TestTemplate
    public void testSumBoxedLongOnJoin(TestDatabase testDb) throws SQLException {
        testNumber(testDb, """
                        SELECT SUM(Invoice.clientId) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        WHERE Client.name LIKE 'Acme%'""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().startsWith("Acme"))
                        .sum((i, _) -> i.clientId()), Long.class);
    }

    // --- float

    @TestTemplate
    public void testSumFloat(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(alt) FROM Town", from(testDb, Town.class)
                .sum(Town::alt), Float.class);
    }

    @TestTemplate
    public void testMinFloat(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(alt) FROM Town", from(testDb, Town.class)
                .min(Town::alt), Float.class);
    }

    @TestTemplate
    public void testMaxFloat(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(alt) FROM Town", from(testDb, Town.class)
                .max(Town::alt), Float.class);
    }

    @TestTemplate
    public void testAvgFloat(TestDatabase testDb) throws SQLException {
        testAvg(testDb, "SELECT AVG(alt) FROM Town", from(testDb, Town.class)
                .aggregate(t -> avg(t.alt())));
    }

    @TestTemplate
    public void testSumFloatWithMultiply(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(alt * 2) FROM Town", from(testDb, Town.class)
                .sum(t -> t.alt() * 2), Float.class);
    }

    @TestTemplate
    public void testMaxFloatWithSubtract(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(ratio - 1) FROM Reading", from(testDb, Reading.class)
                .max(r -> r.ratio() - 1), Float.class);
    }

    @TestTemplate
    public void testMinFloatWithDivide(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(ratio / 2) FROM Reading", from(testDb, Reading.class)
                .min(r -> r.ratio() / 2), Float.class);
    }

    @TestTemplate
    public void testSumFloatWithWhere(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(alt) FROM Town WHERE alt > 100", from(testDb, Town.class)
                .where(t -> t.alt() > 100)
                .sum(Town::alt), Float.class);
    }

    // --- short and byte

    @TestTemplate
    public void testSumShort(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(small) FROM Reading", from(testDb, Reading.class)
                .sum(Reading::small), Short.class);
    }

    @TestTemplate
    public void testMinShort(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(small) FROM Reading", from(testDb, Reading.class)
                .min(Reading::small), Short.class);
    }

    @TestTemplate
    public void testMaxShort(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(small) FROM Reading", from(testDb, Reading.class)
                .max(Reading::small), Short.class);
    }

    @TestTemplate
    public void testAvgShort(TestDatabase testDb) throws SQLException {
        testAvg(testDb, "SELECT AVG(small) FROM Reading", from(testDb, Reading.class)
                .aggregate(r -> avg(r.small())));
    }

    @TestTemplate
    public void testSumByte(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(tiny) FROM Reading", from(testDb, Reading.class)
                .sum(Reading::tiny), Byte.class);
    }

    @TestTemplate
    public void testMinByte(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(tiny) FROM Reading", from(testDb, Reading.class)
                .min(Reading::tiny), Byte.class);
    }

    @TestTemplate
    public void testMaxByte(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MAX(tiny) FROM Reading", from(testDb, Reading.class)
                .max(Reading::tiny), Byte.class);
    }

    @TestTemplate
    public void testAvgByte(TestDatabase testDb) throws SQLException {
        testAvg(testDb, "SELECT AVG(tiny) FROM Reading", from(testDb, Reading.class)
                .aggregate(r -> avg(r.tiny())));
    }

    // --- int (only max is covered elsewhere)

    @TestTemplate
    public void testSumInt(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(age) FROM Person", from(testDb, Person.class)
                .sum(Person::age), Integer.class);
    }

    @TestTemplate
    public void testMinInt(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT MIN(age) FROM Person", from(testDb, Person.class)
                .min(Person::age), Integer.class);
    }

    @TestTemplate
    public void testAvgInt(TestDatabase testDb) throws SQLException {
        testAvg(testDb, "SELECT AVG(age) FROM Person", from(testDb, Person.class)
                .aggregate(p -> avg(p.age())));
    }

    @TestTemplate
    public void testSumIntWithAdd(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT SUM(age + 1) FROM Person", from(testDb, Person.class)
                .sum(p -> p.age() + 1), Integer.class);
    }

    // --- aggregate(), group by and having

    @TestTemplate
    public void testAggregateLongAndFloat(TestDatabase testDb) throws SQLException {
        testRow(testDb, "SELECT SUM(bigNum),MIN(ratio) FROM Reading",
                from(testDb, Reading.class).aggregate(r -> sum(r.bigNum()), r -> min(r.ratio())));
    }

    @TestTemplate
    public void testAggregateLongMinMaxAvg(TestDatabase testDb) throws SQLException {
        testRow(testDb, "SELECT MIN(bigNum),MAX(bigNum),AVG(bigNum) FROM Reading",
                from(testDb, Reading.class)
                        .aggregate(r -> min(r.bigNum()), r -> max(r.bigNum()), r -> avg(r.bigNum())));
    }

    @TestTemplate
    public void testGroupByWithFloatAggregates(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT flag,SUM(ratio),MAX(ratio) FROM Reading GROUP BY flag ORDER BY flag",
                from(testDb, Reading.class)
                        .groupBy(Reading::flag)
                        .orderBy(Reading::flag)
                        .list(Reading::flag, r -> sum(r.ratio()), r -> max(r.ratio())),
                Boolean.class, Float.class, Float.class);
    }

    @TestTemplate
    public void testGroupByWithAvgOfLong(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT flag,AVG(bigNum) FROM Reading GROUP BY flag ORDER BY flag",
                from(testDb, Reading.class)
                        .groupBy(Reading::flag)
                        .orderBy(Reading::flag)
                        .list(Reading::flag, r -> avg(r.bigNum())),
                Boolean.class, Double.class);
    }

    @TestTemplate
    public void testHavingOnLongSum(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT flag,SUM(bigNum) FROM Reading
                        GROUP BY flag HAVING SUM(bigNum) > 4000000000 ORDER BY flag""",
                from(testDb, Reading.class)
                        .groupBy(Reading::flag)
                        .having(r -> sum(r.bigNum()) > 4_000_000_000L)
                        .orderBy(Reading::flag)
                        .list(Reading::flag, r -> sum(r.bigNum())),
                Boolean.class, Long.class);
    }

    @TestTemplate
    public void testHavingOnFloatMax(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT flag,MAX(ratio) FROM Reading
                        GROUP BY flag HAVING MAX(ratio) > 50 ORDER BY flag""",
                from(testDb, Reading.class)
                        .groupBy(Reading::flag)
                        .having(r -> max(r.ratio()) > 50)
                        .orderBy(Reading::flag)
                        .list(Reading::flag, r -> max(r.ratio())),
                Boolean.class, Float.class);
    }

}
