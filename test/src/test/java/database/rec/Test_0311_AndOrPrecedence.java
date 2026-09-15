package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static rec.Person.Sex.FEMALE;
import static rec.Person.Sex.MALE;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0311_AndOrPrecedence extends DatabaseTest {

    // ========== Testing SQL Operator Precedence with And/Or ==========
    // Note: Mixed And/Or precedence should be handled by lambda expressions
    // within where() clause, not by chaining .and() and .or() methods

    @TestTemplate
    public void testChaining_From_And(TestDatabase testDb) throws SQLException {
        // Test that And1 supports further .and() chaining
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
    public void testChaining_From_Or(TestDatabase testDb) throws SQLException {
        // Test that Or1 supports further .or() chaining
        testListOfRecord(testDb, "SELECT * FROM Person WHERE age<15 OR age>70 OR clientId=0 OR name='Alice'",
                from(testDb, Person.class)
                        .where(p -> p.age() < 15)
                        .or(p -> p.age() > 70)
                        .or(p -> p.clientId() == 0)
                        .or(p -> p.name().equals("Alice"))
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhere_And_WithAggregate(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT COUNT(*) FROM Person WHERE sex='MALE' AND age>18",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() > 18)
                        .count(), Long.class
        );
    }

    @TestTemplate
    public void testWhere_Or_WithAggregate(TestDatabase testDb) throws SQLException {
        testNumber(testDb, "SELECT COUNT(*) FROM Person WHERE sex='MALE' OR age<20",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .or(p -> p.age() < 20)
                        .count(), Long.class
        );
    }

    @TestTemplate
    public void testEmpty_Result_With_And(TestDatabase testDb) throws SQLException {
        // Should return empty - no one is both MALE and FEMALE
        var want = select(testDb, "SELECT * FROM Person WHERE 1=0", Person.class);
        var have = from(testDb, Person.class)
                .where(p -> p.sex() == MALE)
                .and(p -> p.sex() == FEMALE)
                .list();
        assertThat(want).isEmpty();
        assertThat(have).isEmpty();
    }

    @TestTemplate
    public void testAll_Results_With_Or(TestDatabase testDb) throws SQLException {
        // Should return all - everyone is either MALE or FEMALE
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' OR sex='FEMALE'",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .or(p -> p.sex() == FEMALE)
                        .list(), Person.class
        );
    }

    // ========== Mixed Lambda Expressions with Chained And/Or ==========

    @TestTemplate
    public void testWhereOrLambda_And_SimpleLambda(TestDatabase testDb) throws SQLException {
        // WHERE (age < 18 OR age > 65) AND sex = 'MALE'
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (age<18 OR age>65) AND sex='MALE'",
                from(testDb, Person.class)
                        .where(p -> p.age() < 18 || p.age() > 65)
                        .and(p -> p.sex() == MALE)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereAndLambda_And_SimpleLambda(TestDatabase testDb) throws SQLException {
        // WHERE (sex = 'MALE' AND age > 18) AND clientId > 0
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (sex='MALE' AND age>18) AND clientId>0",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE && p.age() > 18)
                        .and(p -> p.clientId() > 0)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereOrLambda_Or_SimpleLambda(TestDatabase testDb) throws SQLException {
        // WHERE (age < 18 OR age > 65) OR name = 'Alice'
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (age<18 OR age>65) OR name='Alice'",
                from(testDb, Person.class)
                        .where(p -> p.age() < 18 || p.age() > 65)
                        .or(p -> p.name().equals("Alice"))
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereAndLambda_Or_SimpleLambda(TestDatabase testDb) throws SQLException {
        // WHERE (sex = 'MALE' AND age > 60) OR clientId = 0
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (sex='MALE' AND age>60) OR clientId=0",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE && p.age() > 60)
                        .or(p -> p.clientId() == 0)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereOrLambda_And_OrLambda(TestDatabase testDb) throws SQLException {
        // WHERE (age < 18 OR age > 65) AND (sex = 'MALE' OR sex = 'FEMALE')
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (age<18 OR age>65) AND (sex='MALE' OR sex='FEMALE')",
                from(testDb, Person.class)
                        .where(p -> p.age() < 18 || p.age() > 65)
                        .and(p -> p.sex() == MALE || p.sex() == FEMALE)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testWhereAndLambda_Or_AndLambda(TestDatabase testDb) throws SQLException {
        // WHERE (sex = 'MALE' AND age > 60) OR (sex = 'FEMALE' AND age < 20)
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (sex='MALE' AND age>60) OR (sex='FEMALE' AND age<20)",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE && p.age() > 60)
                        .or(p -> p.sex() == FEMALE && p.age() < 20)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testComplexOrLambda_And_And(TestDatabase testDb) throws SQLException {
        // WHERE (age < 15 OR age > 70 OR clientId = 0) AND sex = 'MALE' AND name LIKE 'Bob%'
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (age<15 OR age>70 OR clientId=0) AND sex='MALE' AND name like 'Bob%'",
                from(testDb, Person.class)
                        .where(p -> p.age() < 15 || p.age() > 70 || p.clientId() == 0)
                        .and(p -> p.sex() == MALE)
                        .and(p -> p.name().startsWith("Bob"))
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testComplexAndLambda_Or_Or(TestDatabase testDb) throws SQLException {
        // WHERE (sex = 'MALE' AND age > 18 AND clientId > 0) OR age < 10 OR name = 'Alice'
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (sex='MALE' AND age>18 AND clientId>0) OR age<10 OR name='Alice'",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE && p.age() > 18 && p.clientId() > 0)
                        .or(p -> p.age() < 10)
                        .or(p -> p.name().equals("Alice"))
                        .list(), Person.class
        );

    }

    @TestTemplate
    public void testNestedMixedLambda_And(TestDatabase testDb) throws SQLException {
        // WHERE ((age > 18 AND age < 30) OR (age > 60 AND age < 80)) AND sex = 'MALE'
        testListOfRecord(testDb, "SELECT * FROM Person WHERE ((age>18 AND age<30) OR (age>60 AND age<80)) AND sex='MALE'",
                from(testDb, Person.class)
                        .where(p -> (p.age() > 18 && p.age() < 30) || (p.age() > 60 && p.age() < 80))
                        .and(p -> p.sex() == MALE)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testNestedMixedLambda_Or(TestDatabase testDb) throws SQLException {
        // WHERE ((sex = 'MALE' OR sex = 'FEMALE') AND age > 18) OR clientId = 0
        testListOfRecord(testDb, "SELECT * FROM Person WHERE ((sex='MALE' OR sex='FEMALE') AND age>18) OR clientId=0",
                from(testDb, Person.class)
                        .where(p -> (p.sex() == MALE || p.sex() == FEMALE) && p.age() > 18)
                        .or(p -> p.clientId() == 0)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testMultipleComplexLambdas_AndChain(TestDatabase testDb) throws SQLException {
        // WHERE (age > 18 OR clientId > 0) AND (sex = 'MALE' OR name LIKE 'B%') AND age < 70
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (age>18 OR clientId>0) AND (sex='MALE' OR name like 'B%') AND age<70",
                from(testDb, Person.class)
                        .where(p -> p.age() > 18 || p.clientId() > 0)
                        .and(p -> p.sex() == MALE || p.name().startsWith("B"))
                        .and(p -> p.age() < 70)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testMultipleComplexLambdas_OrChain(TestDatabase testDb) throws SQLException {
        // WHERE (sex = 'MALE' AND age > 60) OR (sex = 'FEMALE' AND age < 20) OR clientId = 0
        testListOfRecord(testDb, "SELECT * FROM Person WHERE (sex='MALE' AND age>60) OR (sex='FEMALE' AND age<20) OR clientId=0",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE && p.age() > 60)
                        .or(p -> p.sex() == FEMALE && p.age() < 20)
                        .or(p -> p.clientId() == 0)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimple_And_ComplexOrLambda(TestDatabase testDb) throws SQLException {
        // WHERE sex = 'MALE' AND (age < 18 OR age > 65)
        testListOfRecord(testDb, "SELECT * FROM Person WHERE sex='MALE' AND (age<18 OR age>65)",
                from(testDb, Person.class)
                        .where(p -> p.sex() == MALE)
                        .and(p -> p.age() < 18 || p.age() > 65)
                        .list(), Person.class
        );
    }

    @TestTemplate
    public void testSimple_Or_ComplexAndLambda(TestDatabase testDb) throws SQLException {
        // WHERE clientId = 0 OR (sex = 'MALE' AND age > 18 AND age < 30)
        testListOfRecord(testDb, "SELECT * FROM Person WHERE clientId=0 OR (sex='MALE' AND age>18 AND age<30)",
                from(testDb, Person.class)
                        .where(p -> p.clientId() == 0)
                        .or(p -> p.sex() == MALE && p.age() > 18 && p.age() < 30)
                        .list(), Person.class
        );
    }
}

