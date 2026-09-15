package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.util.Set;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0110_IntWhere extends DatabaseTest {

    private static final int staticAge = 20;
    private static final Integer staticAgeObject = 20;
    private final int age = 20;
    private final Integer ageObject = 20;

    private static Integer staticObjectAgeMethod() {
        return 20;
    }

    private static int primitiveObjectAgeMethod() {
        return 20;
    }

    private int primitiveAgeMethod() {
        return 20;
    }

    private Integer objectAgeMethod() {
        return 20;
    }

    @TestTemplate
    public void testIntEqualsConst(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == 20)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntInConst(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age IN(20,30)", from(testDb, Person.class)
                .where(p -> Set.of(20,30).contains(p.age()))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntNotInConst(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age NOT IN(20,30)", from(testDb, Person.class)
                .where(p -> !Set.of(20,30).contains(p.age()))
                .list(), Person.class
        );
    }


    @TestTemplate
    public void testIntEqualsVar(TestDatabase testDb) throws SQLException {
        var age = 20;
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == age)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == age)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == ageObject)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == staticAge)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsStaticObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == staticAgeObject)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == primitiveAgeMethod())
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == objectAgeMethod())
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsStaticObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == staticObjectAgeMethod())
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=20", from(testDb, Person.class)
                .where(p -> p.age() == primitiveObjectAgeMethod())
                .list(), Person.class
        );
    }

    private void testIntEqualsParam(TestDatabase testDb, int age) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age=" + age, from(testDb, Person.class)
                .where(p -> p.age() == age)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntEqualsParam(TestDatabase testDb) throws SQLException {
        testIntEqualsParam(testDb, 20);
        testIntEqualsParam(testDb, 30);
    }

    @TestTemplate
    public void testIntLess(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age<20", from(testDb, Person.class)
                .where(p -> p.age() < 20)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntGreater(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age>20", from(testDb, Person.class)
                .where(p -> p.age() > 20)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntNotEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age<>20", from(testDb, Person.class)
                .where(p -> p.age() != 20)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntLessOrEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age<=20", from(testDb, Person.class)
                .where(p -> p.age() <= 20)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testIntGreaterOrEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age>=20", from(testDb, Person.class)
                .where(p -> p.age() >= 20)
                .list(), Person.class
        );
    }

}
