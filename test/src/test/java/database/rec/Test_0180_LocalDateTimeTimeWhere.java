package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Meeting;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.time.LocalDateTime;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0180_LocalDateTimeTimeWhere extends DatabaseTest {

    private static final LocalDateTime staticField = LocalDateTime.of(2026, 5, 11, 10, 0);
    private final LocalDateTime field = LocalDateTime.of(2026, 5, 11, 10, 0);

    private static LocalDateTime staticMethod() {
        return LocalDateTime.of(2026, 5, 11, 10, 0);
    }

    private LocalDateTime method() {
        return LocalDateTime.of(2026, 5, 11, 10, 0);
    }

    @TestTemplate
    public void testLocalDateTimeEquals(TestDatabase testDb) throws SQLException {
        var date = LocalDateTime.of(2026, 5, 11, 10, 0);
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime='2026-05-11 10:00'", from(testDb, Meeting.class)
                .where(m -> m.startTime().equals(date))
                .list(), Meeting.class
        );
    }

    @TestTemplate
    public void testLocalDateTimeIsAfter(TestDatabase testDb) throws SQLException {
        var date = LocalDateTime.of(2026, 5, 11, 10, 0);
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime>'2026-05-11 10:00'", from(testDb, Meeting.class)
                .where(p -> p.startTime().isAfter(date))
                .list(), Meeting.class
        );
    }

    @TestTemplate
    public void testLocalDateTimeIsBefore(TestDatabase testDb) throws SQLException {
        var date = LocalDateTime.of(2026, 5, 11, 10, 0);
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime<'2026-05-11 10:00'", from(testDb, Meeting.class)
                .where(p -> p.startTime().isBefore(date))
                .list(), Meeting.class
        );
    }

    @TestTemplate
    public void testLocalDateTimeIsNull(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime IS NULL", from(testDb, Meeting.class)
                .where(p -> p.startTime() == null)
                .list(), Meeting.class
        );
    }

    @TestTemplate
    public void testLocalDateTimeIsNotNull(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime IS NOT NULL", from(testDb, Meeting.class)
                .where(p -> p.startTime() != null)
                .list(), Meeting.class
        );
    }

    @TestTemplate
    public void testLocalDateTimeEqualsField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime='2026-05-11 10:00'", from(testDb, Meeting.class)
                .where(p -> p.startTime().equals(field))
                .list(), Meeting.class
        );
    }

    @TestTemplate
    public void testLocalDateTimeEqualsStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime='2026-05-11 10:00'", from(testDb, Meeting.class)
                .where(p -> p.startTime().equals(staticField))
                .list(), Meeting.class
        );
    }

    @TestTemplate
    public void testLocalDateTimeEqualsMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime='2026-05-11 10:00'", from(testDb, Meeting.class)
                .where(p -> p.startTime().equals(method()))
                .list(), Meeting.class
        );
    }

    @TestTemplate
    public void testLocalDateTimeEqualsStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Meeting WHERE startTime='2026-05-11 10:00'", from(testDb, Meeting.class)
                .where(p -> p.startTime().equals(staticMethod()))
                .list(), Meeting.class
        );
    }


}
