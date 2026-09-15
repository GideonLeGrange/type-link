package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static rec.Person.Sex.MALE;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0100_Simple extends DatabaseTest {

    @TestTemplate
    public void testSelect(TestDatabase db) throws SQLException {
        testListOfRecord(db,
                "SELECT * FROM Person", from(db, Person.class)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereEnum(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE'", from(testDb, Person.class)
                .where(p -> p.sex() == MALE)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereInt(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT Person.* FROM Person WHERE age=18", from(testDb, Person.class)
                .where(p -> p.age() == 18)
                .list(), Person.class
        );
    }


    @TestTemplate
    public void testWhereLong(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=18", from(testDb, Person.class)
                .where(p -> p.age() == 18L)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereEnumAndLong(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>18", from(testDb, Person.class)
                .where(p -> p.sex() == MALE && p.age() > 18)
                .list(), Person.class);
    }

    @TestTemplate
    public void testIntNotEq(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age<>18", from(testDb, Person.class).where(person -> person.age() != 18)
                .list(), Person.class
        );
    }

}
