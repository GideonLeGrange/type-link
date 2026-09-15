package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.time.LocalDate;

import static rec.Person.Sex.FEMALE;
import static rec.Person.Sex.MALE;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0300_ComplexWhere extends DatabaseTest {

    @TestTemplate
    public void testWhereAnd(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=25 AND sex='FEMALE'", from(testDb, Person.class)
                .where(person -> person.age() == 25 && person.sex() == FEMALE)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereAndAnd(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=25 AND sex='FEMALE' AND clientId=1", from(testDb, Person.class)
                .where(person -> person.age() == 25 && person.sex() == FEMALE
                        && person.clientId() == 1)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereAnd_And(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=25 AND sex='FEMALE' AND clientId=1", from(testDb, Person.class)
                .where(person -> person.age() == 25 && person.sex() == FEMALE)
                .and(person -> person.clientId() == 1)
                .list(), Person.class
        );
    }


    @TestTemplate
    public void testWhereAnd_AndAnd(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=25 AND sex='FEMALE' AND clientId=1 AND name like 'Alice%'", from(testDb, Person.class)
                .where(person -> person.age() == 25 && person.sex() == FEMALE)
                .and(person -> person.clientId() == 1 && person.name().startsWith("Alice"))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereAndAndAndAnd(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(1994, 5, 11);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=30 AND sex='MALE' AND clientId=1 AND name like 'Bob%' AND birthDay='1994-05-11'",
                from(testDb, Person.class)
                        .where(person -> person.age() == 30
                                && person.sex() == MALE
                                && person.clientId() == 1
                                && person.name().startsWith("Bob")
                                && person.birthDay().equals(date))
                        .list(), Person.class
        );
    }


    @TestTemplate
    public void testWhereOr(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=18 OR sex='FEMALE'", from(testDb, Person.class)
                .where(person -> person.age() == 18 || person.sex() == FEMALE)
                .list(), Person.class
        );
    }


    @TestTemplate
    public void testWhereOrOr(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=18 OR sex='FEMALE' OR clientId=1", from(testDb, Person.class)
                .where(person -> person.age() == 18
                        || person.sex() == FEMALE
                        || person.clientId() == 1)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereOrOrOr(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=18 OR sex='FEMALE' OR clientId=1 OR name='Bob'", from(testDb, Person.class)
                .where(person -> person.age() == 18
                        || person.sex() == FEMALE
                        || person.clientId() == 1
                        || person.name().equals("Bob"))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereOrOrOrOr(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(1972, 4, 4);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=18 OR sex='FEMALE' OR clientId=1 OR name='Bob' OR birthDay='1972-04-04'", from(testDb, Person.class)
                .where(person -> person.age() == 18
                        || person.sex() == FEMALE
                        || person.clientId() == 1
                        || person.name().equals("Bob")
                        || person.birthDay().equals(date))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereOrAnd(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (age=25 OR age=16) AND sex='FEMALE'", from(testDb, Person.class).where(
                        person -> (person.age() == 25 || person.age() == 16)
                                && person.sex() == FEMALE)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereAndOrAnd(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (age>=16 AND sex='FEMALE') OR (age>=18 AND sex='MALE')", from(testDb, Person.class).where(
                        person -> (person.age() >= 16 && person.sex() == FEMALE)
                                || (person.age() >= 18 && person.sex() == MALE))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereOrAndOr(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, """
                        SELECT * FROM Person WHERE email='bob@acme.com'
                        OR email='fred@acme.com'
                        AND (sex='FEMALE' OR sex='MALE')
                        """,
                from(testDb, Person.class).where(person ->
                                person.email().equals("bob@acme.com")
                                        || person.email().equals("fred@acme.com")
                                        && (person.sex() == FEMALE || person.sex() == MALE))
                        .list(), Person.class
        );
    }

}
