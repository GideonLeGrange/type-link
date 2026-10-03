package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Appointment;
import rec.Client;
import rec.Invoice;
import rec.Meeting;
import rec.Person;
import rec.Reading;

import java.sql.SQLException;

import static me.legrange.typelink.Selects.count;
import static me.legrange.typelink.Selects.sum;

/**
 * Ordering combined with other clauses, with column types other than numbers and strings, and with expressions.
 * Where a column has duplicates or nulls the ordering is finished with the id so that the result is deterministic. The
 * control queries run on the same database as the query under test, so null ordering is not asserted on directly.
 */
@SuppressWarnings({"NewClassNamingConvention", "Convert2MethodRef"})
public final class Test_0420_OrderByCombinations extends DatabaseTest {

    // --- order by combined with limit, distinct, having, where, subqueries

    @TestTemplate
    public void testOrderByLimit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age, id LIMIT 3", from(testDb, Person.class)
                .orderBy(Person::age)
                .thenBy(Person::id)
                .limit(3)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByDescendingLimitOffset(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age DESC, id DESC LIMIT 3 OFFSET 2", from(testDb, Person.class)
                .orderByDescending(Person::age)
                .thenByDescending(Person::id)
                .limit(3, 2)
                .list(), Person.class);
    }

    @TestTemplate
    public void testWhereOrderByLimit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age > 18 ORDER BY age, id LIMIT 4", from(testDb, Person.class)
                .where(p -> p.age() > 18)
                .orderBy(Person::age)
                .thenBy(Person::id)
                .limit(4)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByLimitTwoTables(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name, Invoice.id LIMIT 4""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((_, c) -> c.name())
                        .thenBy((i, _) -> i.id())
                        .limit(4)
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByLimitThreeTables(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Person.name, Invoice.id, Person.id LIMIT 5 OFFSET 3""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((_, _, p) -> p.name())
                        .thenBy((i, _, _) -> i.id())
                        .thenBy((_, _, p) -> p.id())
                        .limit(5, 3)
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testDistinctOrderBy(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT DISTINCT age FROM Person ORDER BY age DESC", Integer.class);
        var have = from(testDb, Person.class)
                .distinct()
                .orderByDescending(Person::age)
                .list(Person::age);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testDistinctOrderByLimit(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT DISTINCT clientId FROM Invoice ORDER BY clientId DESC LIMIT 2", Long.class);
        var have = from(testDb, Invoice.class)
                .distinct()
                .orderByDescending(Invoice::clientId)
                .limit(2)
                .list(Invoice::clientId);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testGroupByHavingOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT clientId, SUM(amount) FROM Invoice
                        GROUP BY clientId HAVING COUNT(*) > 1
                        ORDER BY SUM(amount) DESC""",
                from(testDb, Invoice.class)
                        .groupBy(Invoice::clientId)
                        .having(i -> count(i) > 1)
                        .orderByDescending(i -> sum(i.amount()))
                        .list(Invoice::clientId, i -> sum(i.amount())),
                Long.class, Double.class);
    }

    @TestTemplate
    public void testGroupByHavingOrderByLimitTwoTables(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT Client.name, SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        GROUP BY Client.name HAVING SUM(Invoice.amount) > 100
                        ORDER BY SUM(Invoice.amount), Client.name LIMIT 3""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .groupBy((_, c) -> c.name())
                        .having((i, _) -> sum(i.amount()) > 100)
                        .orderBy((i, _) -> sum(i.amount()))
                        .thenBy((_, c) -> c.name())
                        .limit(3)
                        .list((_, c) -> c.name(), (i, _) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testOrderByWithSubQueryInWhere(TestDatabase testDb) throws SQLException {
        var want = select(testDb, """
                SELECT * FROM Invoice WHERE Invoice.amount >
                (SELECT AVG(Invoice.amount) FROM Invoice) ORDER BY clientId DESC, id""", Invoice.class);
        var db = db(testDb);
        var have = db.from(Invoice.class)
                .where(i -> i.amount() > db.from(Invoice.class).avg(Invoice::amount))
                .orderByDescending(Invoice::clientId)
                .thenBy(Invoice::id)
                .list();
        assertExpected(want, have);
    }

    // --- direction

    @TestTemplate
    public void testOrderByDescendingThenByDescendingSingleTable(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age DESC, name DESC, id DESC", from(testDb, Person.class)
                .orderByDescending(Person::age)
                .thenByDescending(Person::name)
                .thenByDescending(Person::id)
                .list(), Person.class);
    }

    // --- column types

    @TestTemplate
    public void testOrderByEnum(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY sex, id", from(testDb, Person.class)
                .orderBy(Person::sex)
                .thenBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByEnumDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY sex DESC, id", from(testDb, Person.class)
                .orderByDescending(Person::sex)
                .thenBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByLocalDate(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice ORDER BY invoiceDate DESC, id", from(testDb, Invoice.class)
                .orderByDescending(Invoice::invoiceDate)
                .thenBy(Invoice::id)
                .list(), Invoice.class);
    }

    @TestTemplate
    public void testOrderByNullableLocalDate(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay IS NOT NULL ORDER BY birthDay, id", from(testDb, Person.class)
                .where(p -> p.birthDay() != null)
                .orderBy(Person::birthDay)
                .thenBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByNullableString(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY email, id", from(testDb, Person.class)
                .orderBy(Person::email)
                .thenBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByLocalDateTime(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime IS NOT NULL ORDER BY startTime DESC", from(testDb, Meeting.class)
                .where(m -> m.startTime() != null)
                .orderByDescending(Meeting::startTime)
                .list(), Meeting.class);
    }

    @TestTemplate
    public void testOrderByDate(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime IS NOT NULL ORDER BY startTime DESC", from(testDb, Appointment.class)
                .where(a -> a.startTime() != null)
                .orderByDescending(Appointment::startTime)
                .list(), Appointment.class);
    }

    // --- expressions

    @TestTemplate
    public void testOrderByIntExpression(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age * 2, id", from(testDb, Person.class)
                .orderBy(p -> p.age() * 2)
                .thenBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByNegatedIntExpression(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY -age, id", from(testDb, Person.class)
                .orderBy(p -> -p.age())
                .thenBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByDoubleExpression(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice ORDER BY amount + 1, id", from(testDb, Invoice.class)
                .orderBy(i -> i.amount() + 1)
                .thenBy(Invoice::id)
                .list(), Invoice.class);
    }

    @TestTemplate
    public void testOrderByLongExpression(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY bigNum * 2", from(testDb, Reading.class)
                .orderBy(r -> r.bigNum() * 2)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByNegatedLongExpression(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY -bigNum", from(testDb, Reading.class)
                .orderBy(r -> -r.bigNum())
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByFloatExpression(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY ratio * 2", from(testDb, Reading.class)
                .orderBy(r -> r.ratio() * 2)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByExpressionAcrossTables(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Invoice.amount - Client.id, Invoice.id""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((i, c) -> i.amount() - c.id())
                        .thenBy((i, _) -> i.id())
                        .list((i, _) -> i),
                Invoice.class);
    }

}
