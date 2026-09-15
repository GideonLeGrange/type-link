package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static rec.Person.Sex.MALE;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0310_AndOrSemantics extends DatabaseTest {

    // ========== Single Table Tests (From1) ==========

    @TestTemplate
    public void testWhere_And_Simple(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>18",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > 18)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_And_And(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>18 AND age<70",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > 18)
                        .and(p -> p.age() < 70)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_And_And_And(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>18 AND age<70 AND clientId>0",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > 18)
                        .and(p -> p.age() < 70)
                        .and(p -> p.clientId() > 0)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_Or_Simple(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' OR age<20",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .or(p -> p.age() < 20)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_Or_Or(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' OR age<20 OR clientId=0",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .or(p -> p.age() < 20)
                        .or(p -> p.clientId() == 0)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_Or_Or_Or(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' OR age<20 OR clientId=0 OR name='Alice'",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .or(p -> p.age() < 20)
                        .or(p -> p.clientId() == 0)
                        .or(p -> p.name().equals("Alice"))
                        .list(), Person.class
        );
    }

    // Mixed And/Or should use lambda expressions within where(), not chaining

    @TestTemplate
    public void testAnd_And_ChainedAfterAnd(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>18 AND age<70",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > 18)
                        .and(p -> p.age() < 70)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testOr_Or_ChainedAfterOr(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' OR age<20 OR clientId=0",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .or(p -> p.age() < 20)
                        .or(p -> p.clientId() == 0)
                        .list(), Person.class
        );
    }


    @TestTemplate
    public void testWhere_And_WithOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>18 ORDER BY age",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > 18)
                        .orderBy(p -> p.age())
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_Or_WithOrderBy(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' OR age<20 ORDER BY age",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .or(p -> p.age() < 20)
                        .orderBy(p -> p.age())
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_And_WithLimit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND age>18 LIMIT 3",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > 18)
                        .limit(3)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_Or_WithLimit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' OR age<20 LIMIT 3",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .or(p -> p.age() < 20)
                        .limit(3)
                        .list(), Person.class
        );
    }
}

