package database.bean;

import database.testing.BeanDatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import bean.Person;

import java.sql.SQLException;

import static bean.Person.Sex.MALE;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0100_Simple extends BeanDatabaseTest {

    @TestTemplate
    public void testSelect(TestDatabase db) throws SQLException {
        testListOfRecord(db,
                "SELECT * FROM Person", from(db, Person.class)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereId(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE id=1", from(testDb, Person.class)
                .where(p -> p.getId() == 1)
                .list(), Person.class
        );
    }


    @TestTemplate
    public void testWhereEnum(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE'", from(testDb, Person.class)
                .where(p -> p.getSex() == MALE)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereInt(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT Person.* FROM Person WHERE age=18", from(testDb, Person.class)
                .where(p -> p.getAge() == 18)
                .list(), Person.class
        );
    }


    @TestTemplate
    public void testWhereLong(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=18", from(testDb, Person.class)
                .where(p -> p.getAge() == 18L)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereEnumAndLong(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>18", from(testDb, Person.class)
                .where(p -> p.getSex() == MALE && p.getAge() > 18)
                .list(), Person.class);
    }

    @TestTemplate
    public void testIntNotEq(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age<>18", from(testDb, Person.class).where(person -> person.getAge() != 18)
                .list(), Person.class
        );
    }

}
