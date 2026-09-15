package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0160_StringWhere extends DatabaseTest {

    private static final String staticField = "Bob Jones";
    private final String field = "Bob Jones";

    private static String staticMethod() {
        return "Bob Jones";
    }

    private String method() {
        return "Bob Jones";
    }

    @TestTemplate
    public void testSimpleWhereWithStringEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name = 'Bob Jones'", from(testDb, Person.class)
                .where(p -> p.name().equals("Bob Jones"))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereWithStringNotEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name <> 'Bob Jones'", from(testDb, Person.class)
                .where(p -> !p.name().equals("Bob Jones"))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereWithStringStartsWith(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name LIKE 'Bob%'", from(testDb, Person.class)
                .where(p -> p.name().startsWith("Bob"))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereWithStringNotStartsWith(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name NOT LIKE 'Bob%'", from(testDb, Person.class)
                .where(p -> !p.name().startsWith("Bob"))
                .list(), Person.class
        );
    }


    @TestTemplate
    public void testSimpleWhereWithStringEndsWith(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name LIKE '%Jones'", from(testDb, Person.class)
                .where(p -> p.name().endsWith("Jones"))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereWithStringNotEndsWith(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name NOT LIKE '%Jones'", from(testDb, Person.class)
                .where(p -> !p.name().endsWith("Jones"))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereWithStringContains(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name LIKE '%ob%'", from(testDb, Person.class)
                .where(p -> p.name().contains("ob"))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereWithStringNotContains(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name NOT LIKE '%ob%'", from(testDb, Person.class)
                .where(p -> !p.name().contains("ob"))
                .list(), Person.class
        );
    }


    @TestTemplate
    public void testStringIsNull(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE email IS NULL", from(testDb, Person.class)
                .where(p -> p.email() == null)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testStringIsNotNull(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE email IS NOT NULL", from(testDb, Person.class)
                .where(p -> p.email() != null)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name = 'Bob Jones'", from(testDb, Person.class)
                .where(p -> p.name().equals(field))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name = 'Bob Jones'", from(testDb, Person.class)
                .where(p -> p.name().equals(method()))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name = 'Bob Jones'", from(testDb, Person.class)
                .where(p -> p.name().equals(staticField))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimpleWhereStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE name = 'Bob Jones'", from(testDb, Person.class)
                .where(p -> p.name().equals(staticMethod()))
                .list(), Person.class
        );
    }

}
