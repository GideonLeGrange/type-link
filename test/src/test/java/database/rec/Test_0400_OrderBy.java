package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0400_OrderBy extends DatabaseTest {

    @TestTemplate
    public void testOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY name", from(testDb, Person.class)
                .orderBy(Person::name)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereAndOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE email IS NOT NULL ORDER BY name",
                from(testDb, Person.class)
                        .where(p -> p.email() != null)
                        .orderBy(Person::name)
                        .list(), Person.class
        );
    }


    @TestTemplate
    public void testOrderByThenBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age, name", from(testDb, Person.class)
                .orderBy(Person::age)
                .thenBy(Person::name)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testOrderByDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY name DESC", from(testDb, Person.class)
                .orderByDescending(Person::name)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testOrderByThenByDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person ORDER BY age, name DESC", from(testDb, Person.class)
                .orderBy(Person::age)
                .thenByDescending(Person::name)
                .list(), Person.class
        );
    }

}
