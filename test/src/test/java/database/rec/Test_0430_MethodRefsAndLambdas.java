package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Client;
import rec.Invoice;
import rec.Person;

import java.sql.SQLException;

import static me.legrange.typelink.Selects.count;
import static me.legrange.typelink.Selects.sum;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Method references and lambdas compile to different bytecode, and lambdas on joined tables can use any subset of their
 * parameters in any order. These tests combine the two styles across clauses and arities, which the other tests mostly
 * keep apart: method references for single-table queries, lambdas for joins.
 */
@SuppressWarnings({"NewClassNamingConvention", "Convert2MethodRef"})
public final class Test_0430_MethodRefsAndLambdas extends DatabaseTest {

    private boolean isAdult(Person person) {
        return person.age() > 18;
    }

    private static boolean isOld(Person person) {
        return person.age() > 60;
    }

    private static int sortKey(Person person) {
        return person.age();
    }

    // --- one table: boolean method references in where, and, or

    @TestTemplate
    public void testAndWithMethodRef(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount > 100 AND paid = true ORDER BY id", from(testDb, Invoice.class)
                .where(i -> i.amount() > 100)
                .and(Invoice::paid)
                .orderBy(Invoice::id)
                .list(), Invoice.class);
    }

    @TestTemplate
    public void testOrWithMethodRef(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount > 900 OR paid = true ORDER BY id", from(testDb, Invoice.class)
                .where(i -> i.amount() > 900)
                .or(Invoice::paid)
                .orderBy(Invoice::id)
                .list(), Invoice.class);
    }

    @TestTemplate
    public void testWhereMethodRefThenOrLambdaThenOrMethodRef(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid = true OR amount < 100 OR paid = false ORDER BY id", from(testDb, Invoice.class)
                .where(Invoice::paid)
                .or(i -> i.amount() < 100)
                .or(i -> !i.paid())
                .orderBy(Invoice::id)
                .list(), Invoice.class);
    }

    @TestTemplate
    public void testNegatedBooleanInLambda(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid = false ORDER BY id", from(testDb, Invoice.class)
                .where(i -> !i.paid())
                .orderBy(Invoice::id)
                .list(), Invoice.class);
    }

    // --- one table: lambdas where only method references are otherwise used

    @TestTemplate
    public void testOrderByThenByWithLambdas(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age, name, id", from(testDb, Person.class)
                .orderBy(p -> p.age())
                .thenBy(p -> p.name())
                .thenBy(p -> p.id())
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByDescendingThenByDescendingWithLambdas(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age DESC, name DESC, id DESC", from(testDb, Person.class)
                .orderByDescending(p -> p.age())
                .thenByDescending(p -> p.name())
                .thenByDescending(p -> p.id())
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByMethodRefThenByLambda(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age DESC, name, id", from(testDb, Person.class)
                .orderByDescending(Person::age)
                .thenBy(p -> p.name())
                .thenBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testOrderByLambdaThenByMethodRefDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY name, age DESC, id", from(testDb, Person.class)
                .orderBy(p -> p.name())
                .thenByDescending(Person::age)
                .thenBy(p -> p.id())
                .list(), Person.class);
    }

    @TestTemplate
    public void testListMixingLambdasAndMethodRefs(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT name, age, email FROM Person ORDER BY id", from(testDb, Person.class)
                        .orderBy(Person::id)
                        .list(Person::name, p -> p.age(), Person::email),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testListWithOnlyLambdas(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT name, age FROM Person ORDER BY id", from(testDb, Person.class)
                        .orderBy(p -> p.id())
                        .list(p -> p.name(), p -> p.age()),
                String.class, Integer.class);
    }

    @TestTemplate
    public void testGroupByLambdaWithMethodRefProjection(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT clientId, COUNT(*) FROM Invoice GROUP BY clientId ORDER BY clientId",
                from(testDb, Invoice.class)
                        .groupBy(i -> i.clientId())
                        .orderBy(Invoice::clientId)
                        .list(Invoice::clientId, count()),
                Long.class, Long.class);
    }

    @TestTemplate
    public void testGroupByMethodRefsWithLambdaHavingAndOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT clientId, invoiceDate, SUM(amount) FROM Invoice
                        GROUP BY clientId, invoiceDate HAVING SUM(amount) > 500
                        ORDER BY clientId, invoiceDate""",
                from(testDb, Invoice.class)
                        .groupBy(Invoice::clientId, Invoice::invoiceDate)
                        .having(i -> sum(i.amount()) > 500)
                        .orderBy(Invoice::clientId)
                        .thenBy(i -> i.invoiceDate())
                        .list(Invoice::clientId, i -> i.invoiceDate(), i -> sum(i.amount())),
                Long.class, java.time.LocalDate.class, Double.class);
    }

    // --- one table: predicate and key methods that are not record accessors

    @TestTemplate
    public void testWhereWithBoundMethodRef(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age > 18 ORDER BY id", from(testDb, Person.class)
                .where(this::isAdult)
                .orderBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testWhereWithStaticMethodRef(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age > 60 ORDER BY id", from(testDb, Person.class)
                .where(Test_0430_MethodRefsAndLambdas::isOld)
                .orderBy(Person::id)
                .list(), Person.class);
    }

    @TestTemplate
    public void testAndWithBoundAndStaticMethodRefs(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age > 18 AND age > 60 ORDER BY id", from(testDb, Person.class)
                .where(this::isAdult)
                .and(Test_0430_MethodRefsAndLambdas::isOld)
                .orderBy(Person::id)
                .list(), Person.class);
    }

    /** These have no SQL translation. What matters is that they fail instead of returning rows in the wrong order. */
    @TestTemplate
    public void testOrderByStaticMethodRefIsRefused(TestDatabase testDb) {
        assertThrows(Exception.class, () -> from(testDb, Person.class)
                .orderBy(Test_0430_MethodRefsAndLambdas::sortKey)
                .list());
    }

    @TestTemplate
    public void testOrderByObjectMethodRefIsRefused(TestDatabase testDb) {
        assertThrows(Exception.class, () -> from(testDb, Person.class)
                .orderBy(Person::hashCode)
                .list());
    }

    @TestTemplate
    public void testProjectionOfObjectMethodRefIsRefused(TestDatabase testDb) {
        assertThrows(Exception.class, () -> from(testDb, Person.class)
                .list(Person::toString));
    }

    // --- two tables: which parameters a lambda uses

    @TestTemplate
    public void testWhereUsingOnlySecondTable(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice JOIN Client ON Invoice.clientId = Client.id
                        WHERE Client.name = 'Coyote Inc' ORDER BY Invoice.id""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .where((_, c) -> c.name().equals("Coyote Inc"))
                        .orderBy((i, _) -> i.id())
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testOrderByUsingOnlySecondTable(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Client.name DESC, Invoice.id""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderByDescending((_, c) -> c.name())
                        .thenBy((i, _) -> i.id())
                        .list((i, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testListProjectingOnlySecondTable(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT Client.name, Client.id FROM Invoice JOIN Client ON Invoice.clientId = Client.id
                        ORDER BY Invoice.id""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .orderBy((i, _) -> i.id())
                        .list((_, c) -> c.name(), (_, c) -> c.id()),
                String.class, Long.class);
    }

    @TestTemplate
    public void testLambdaParametersWithUnusualNames(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT Client.name, Invoice.amount FROM Invoice JOIN Client ON Invoice.clientId = Client.id
                        WHERE Invoice.amount > 500 ORDER BY Client.name, Invoice.amount""",
                from(testDb, Invoice.class)
                        .join(Client.class, (inv, cl) -> inv.clientId().equals(cl.id()))
                        .where((x, y) -> x.amount() > 500)
                        .orderBy((x, y) -> y.name())
                        .thenBy((x, y) -> x.amount())
                        .list((x, y) -> y.name(), (x, y) -> x.amount()),
                String.class, Double.class);
    }

    // --- three tables: which parameters a lambda uses

    @TestTemplate
    public void testWhereUsingOnlyThirdTable(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        WHERE Person.age > 60 ORDER BY Invoice.id, Person.id""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .where((_, _, p) -> p.age() > 60)
                        .orderBy((i, _, _) -> i.id())
                        .thenBy((_, _, p) -> p.id())
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testJoinPredicateSkippingTheMiddleTable(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Invoice.clientId = Person.clientId
                        ORDER BY Invoice.id, Person.id""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (i, _, p) -> i.clientId().equals(p.clientId()))
                        .orderBy((i, _, _) -> i.id())
                        .thenBy((_, _, p) -> p.id())
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testThenByUsingOnlyMiddleTable(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT Invoice.* FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Person.id, Client.name DESC, Invoice.id""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((_, _, p) -> p.id())
                        .thenByDescending((_, c, _) -> c.name())
                        .thenBy((i, _, _) -> i.id())
                        .list((i, _, _) -> i),
                Invoice.class);
    }

    @TestTemplate
    public void testGroupByThirdTableWithAggregateOnFirst(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT Person.name, SUM(Invoice.amount) FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        GROUP BY Person.name ORDER BY Person.name""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .groupBy((_, _, p) -> p.name())
                        .orderBy((_, _, p) -> p.name())
                        .list((_, _, p) -> p.name(), (i, _, _) -> sum(i.amount())),
                String.class, Double.class);
    }

    @TestTemplate
    public void testListProjectingOnlyMiddleAndThirdTables(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, """
                        SELECT Client.name, Person.name FROM Invoice
                        JOIN Client ON Invoice.clientId = Client.id
                        JOIN Person ON Client.id = Person.clientId
                        ORDER BY Invoice.id, Person.id""",
                from(testDb, Invoice.class)
                        .join(Client.class, (i, c) -> i.clientId().equals(c.id()))
                        .join(Person.class, (_, c, p) -> c.id().equals(p.clientId()))
                        .orderBy((i, _, _) -> i.id())
                        .thenBy((_, _, p) -> p.id())
                        .list((_, c, _) -> c.name(), (_, _, p) -> p.name()),
                String.class, String.class);
    }

}
