package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Person;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.time.LocalDate;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0190_LocalDateWhere extends DatabaseTest {

    private static final LocalDate staticField = LocalDate.of(1994, 5, 11);
    private final LocalDate field = LocalDate.of(1994, 5, 11);

    private static LocalDate staticMethod() {
        return LocalDate.of(1994, 5, 11);
    }

    private LocalDate method() {
        return LocalDate.of(1994, 5, 11);
    }

    @TestTemplate
    public void testLocalDateEquals(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(1994, 5, 11);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay='1994-05-11'", from(testDb, Person.class)
                .where(p -> p.birthDay().equals(date))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateIsAfter(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(1972, 1, 1);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay>'1972-01-01'", from(testDb, Person.class)
                .where(p -> p.birthDay().isAfter(date))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateIsBefore(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(2000, 1, 1);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay<'2000-01-01'", from(testDb, Person.class)
                .where(p -> p.birthDay().isBefore(date))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateIsNull(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay IS NULL", from(testDb, Person.class)
                .where(p -> p.birthDay() == null)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateIsNotNull(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay IS NOT NULL", from(testDb, Person.class)
                .where(p -> p.birthDay() != null)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateEqualsField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay='1994-05-11'", from(testDb, Person.class)
                .where(p -> p.birthDay().equals(field))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateEqualsStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay='1994-05-11'", from(testDb, Person.class)
                .where(p -> p.birthDay().equals(staticField))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateEqualsMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay='1994-05-11'", from(testDb, Person.class)
                .where(p -> p.birthDay().equals(method()))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateEqualsStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay='1994-05-11'", from(testDb, Person.class)
                .where(p -> p.birthDay().equals(staticMethod()))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateEqualsLambdaCall(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay='1994-05-11'", from(testDb, Person.class)
                .where(p -> p.birthDay().equals(LocalDate.of(1994, 5, 11)))
                .list(), Person.class
        );
    }

    // Negated and combined comparisons. These reach Flip, which has to invert isAfter/isBefore
    // rather than return them unchanged.

    @TestTemplate
    public void testLocalDateNotIsAfter(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(1972, 1, 1);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay<='1972-01-01'", from(testDb, Person.class)
                .where(p -> !p.birthDay().isAfter(date))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateNotIsBefore(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(2000, 1, 1);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay>='2000-01-01'", from(testDb, Person.class)
                .where(p -> !p.birthDay().isBefore(date))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateIsAfterAndIsBefore(TestDatabase testDb) throws SQLException {
        var from = LocalDate.of(1972, 1, 1);
        var to = LocalDate.of(2000, 1, 1);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay>'1972-01-01' AND birthDay<'2000-01-01'", from(testDb, Person.class)
                .where(p -> p.birthDay().isAfter(from) && p.birthDay().isBefore(to))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateIsBeforeOrIsAfter(TestDatabase testDb) throws SQLException {
        var from = LocalDate.of(1972, 1, 1);
        var to = LocalDate.of(2000, 1, 1);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay<'1972-01-01' OR birthDay>'2000-01-01'", from(testDb, Person.class)
                .where(p -> p.birthDay().isBefore(from) || p.birthDay().isAfter(to))
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateNotIsAfterAndAge(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(1972, 1, 1);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay<='1972-01-01' AND age>70", from(testDb, Person.class)
                .where(p -> !p.birthDay().isAfter(date) && p.age() > 70)
                .list(), Person.class
        );
    }

    @TestTemplate
    public void testLocalDateNotIsBeforeOrAge(TestDatabase testDb) throws SQLException {
        var date = LocalDate.of(2000, 1, 1);
        testListOfRecord(testDb, "SELECT * FROM Person WHERE birthDay>='2000-01-01' OR age>70", from(testDb, Person.class)
                .where(p -> !p.birthDay().isBefore(date) || p.age() > 70)
                .list(), Person.class
        );
    }

}
