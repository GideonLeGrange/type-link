package database.bean;

import bean.Bean;
import database.testing.BeanDatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import bean.Invoice;
import bean.Person;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Tests for Limit1 multi-column projection methods that return Row2 and Row3
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0101_SimpleProjections extends BeanDatabaseTest {

    @TestTemplate
    public void testSelectTwoColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age FROM Person ORDER BY name",
                from(testDb, Person.class)
                        .orderBy(Person::getName)
                        .list(Person::getName, Person::getAge),
                String.class, Integer.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, email FROM Person WHERE age > 18 ORDER BY name",
                from(testDb, Person.class)
                        .where(p -> p.getAge() > 18)
                        .orderBy(Person::getName)
                        .list(Person::getName, Person::getEmail),
                String.class, String.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsFromInvoice(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT clientId, amount FROM Invoice ORDER BY id",
                from(testDb, Invoice.class)
                        .orderBy(Invoice::getId)
                        .list(Invoice::getClientId, Invoice::getAmount),
                Long.class, Double.class);
    }

    @TestTemplate
    public void testSelectThreeColumns(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age, email FROM Person ORDER BY name",
                from(testDb, Person.class)
                        .orderBy(Person::getName)
                        .list(Person::getName, Person::getAge, Person::getEmail),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsWithWhere(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age, email FROM Person WHERE age >= 18 ORDER BY age, name",
                from(testDb, Person.class)
                        .where(p -> p.getAge() >= 18)
                        .orderBy(Person::getAge)
                        .thenBy(Person::getName)
                        .list(Person::getName, Person::getAge, Person::getEmail),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsFromInvoice(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT description, amount, invoiceDate FROM Invoice ORDER BY invoiceDate, id",
                from(testDb, Invoice.class)
                        .orderBy(Invoice::getInvoiceDate)
                        .thenBy(Invoice::getId)
                        .list(Invoice::getDescription, Invoice::getAmount, Invoice::getInvoiceDate),
                String.class, Double.class, LocalDate.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsWithLimit(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age, email FROM Person ORDER BY name LIMIT 5",
                from(testDb, Person.class)
                        .orderBy(Person::getName)
                        .limit(5)
                        .list(Person::getName, Person::getAge, Person::getEmail),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsWithLimitAndOffset(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age, email FROM Person ORDER BY name LIMIT 3 OFFSET 2",
                from(testDb, Person.class)
                        .orderBy(Person::getName)
                        .limit(3, 2)
                        .list(Person::getName, Person::getAge, Person::getEmail),
                String.class, Integer.class, String.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsWithComplexWhere(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT name, age FROM Person WHERE age > 18 AND email IS NOT NULL ORDER BY age DESC",
                from(testDb, Person.class)
                        .where(p -> p.getAge() > 18 && p.getEmail() != null)
                        .orderByDescending(Person::getAge)
                        .list(Person::getName, Person::getAge),
                String.class, Integer.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsNumericTypes(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT id, amount FROM Invoice WHERE amount > 100 ORDER BY amount",
                from(testDb, Invoice.class)
                        .where(i -> i.getAmount() > 100)
                        .orderBy(Invoice::getAmount)
                        .list(Invoice::getId, Invoice::getAmount),
                Long.class, Double.class);
    }

    @TestTemplate
    public void testSelectTwoColumnsBooleanAndDecimal(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT paid, amount FROM Invoice ORDER BY amount",
                from(testDb, Invoice.class)
                        .orderBy(Invoice::getAmount)
                        .list(Invoice::isPaid, Invoice::getAmount),
                Boolean.class, Double.class);
    }

    @TestTemplate
    public void testSelectThreeColumnsWithDate(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(2023, 12, 31);
        testListOfRow(testDb,
                "SELECT clientId, invoiceDate, amount FROM Invoice WHERE invoiceDate >= '2024-01-01' ORDER BY invoiceDate",
                from(testDb, Invoice.class)
                        .where(i -> i.getInvoiceDate().isAfter(date))
                        .orderBy(Invoice::getInvoiceDate)
                        .list(Invoice::getClientId, Invoice::getInvoiceDate, Invoice::getAmount),
                Long.class, LocalDate.class, Double.class);
    }

        @TestTemplate
        public void testSelectThreeColumnsAllFromPerson(TestDatabase testDb) throws SQLException {
            testListOfRow(testDb,
                    "SELECT id, name, clientId FROM Person ORDER BY id",
                    from(testDb, Person.class)
                            .orderBy(Person::getId)
                            .list(Person::getId, Person::getName, Person::getClientId),
                    Long.class, String.class, Long.class);
        }
}

