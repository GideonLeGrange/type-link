package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Invoice;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Tests for Limit1 multi-column projection methods that return Row2 and Row3
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0101_SimpleProjections extends DatabaseTest {

    @TestTemplate
    public void testSelectTwoColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age FROM Person ORDER BY name",
                from(testDb, Person.class)
                        .orderBy(Person::name)
                        .list(Person::name, Person::age),
                String.class, Integer.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, email FROM Person WHERE age > 18 ORDER BY name",
                from(testDb, Person.class)
                        .where(p -> p.age() > 18)
                        .orderBy(Person::name)
                        .list(Person::name, Person::email),
                String.class, String.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsFromInvoice(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT clientId, amount FROM Invoice ORDER BY id",
                from(testDb, Invoice.class)
                        .orderBy(Invoice::id)
                        .list(Invoice::clientId, Invoice::amount),
                Long.class, Double.class);
    }

    @TestTemplate
    public void testSelectThreeColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age, email FROM Person ORDER BY name",
                from(testDb, Person.class)
                        .orderBy(Person::name)
                        .list(Person::name, Person::age, Person::email),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age, email FROM Person WHERE age >= 18 ORDER BY age, name",
                from(testDb, Person.class)
                        .where(p -> p.age() >= 18)
                        .orderBy(Person::age)
                        .thenBy(Person::name)
                        .list(Person::name, Person::age, Person::email),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsFromInvoice(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT description, amount, invoiceDate FROM Invoice ORDER BY invoiceDate, id",
                from(testDb, Invoice.class)
                        .orderBy(Invoice::invoiceDate)
                        .thenBy(Invoice::id)
                        .list(Invoice::description, Invoice::amount, Invoice::invoiceDate),
                String.class, Double.class, LocalDate.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsWithLimit(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age, email FROM Person ORDER BY name LIMIT 5",
                from(testDb, Person.class)
                        .orderBy(Person::name)
                        .limit(5)
                        .list(Person::name, Person::age, Person::email),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsWithLimitAndOffset(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age, email FROM Person ORDER BY name LIMIT 3 OFFSET 2",
                from(testDb, Person.class)
                        .orderBy(Person::name)
                        .limit(3, 2)
                        .list(Person::name, Person::age, Person::email),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsWithComplexWhere(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age FROM Person WHERE age > 18 AND email IS NOT NULL ORDER BY age DESC",
                from(testDb, Person.class)
                        .where(p -> p.age() > 18 && p.email() != null)
                        .orderByDescending(Person::age)
                        .list(Person::name, Person::age),
                String.class, Integer.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsNumericTypes(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT id, amount FROM Invoice WHERE amount > 100 ORDER BY amount",
                from(testDb, Invoice.class)
                        .where(i -> i.amount() > 100)
                        .orderBy(Invoice::amount)
                        .list(Invoice::id, Invoice::amount),
                Long.class, Double.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsBooleanAndDecimal(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT paid, amount FROM Invoice ORDER BY amount",
                from(testDb, Invoice.class)
                        .orderBy(Invoice::amount)
                        .list(Invoice::paid, Invoice::amount),
                Boolean.class, Double.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsWithDate(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(2023, 12, 31);
        testListOfRow(testDb,
                "SELECT clientId, invoiceDate, amount FROM Invoice WHERE invoiceDate >= '2024-01-01' ORDER BY invoiceDate",
                from(testDb, Invoice.class)
                        .where(i -> i.invoiceDate().isAfter(date))
                        .orderBy(Invoice::invoiceDate)
                        .list(Invoice::clientId, Invoice::invoiceDate, Invoice::amount),
                Long.class, LocalDate.class, Double.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsAllFromPerson(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT id, name, clientId FROM Person ORDER BY id",
                from(testDb, Person.class)
                        .orderBy(Person::id)
                        .list(Person::id, Person::name, Person::clientId),
                Long.class, String.class, Long.class);
    }

    @TestTemplate
    public void testSelectWithStringConcat(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, email, CONCAT(name, ' <', email, '>') AS contact FROM Person ORDER BY name",
                from(testDb, Person.class)
                        .orderBy(Person::name)
                        .list(Person::name, Person::email, p -> p.name() + " <" + p.email() + ">"),
                 String.class);

    }
}

