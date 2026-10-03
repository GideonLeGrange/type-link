package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Appointment;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

import static database.testing.TestData.date;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0185_DateWhere extends DatabaseTest {

    private static final Date staticField = date(LocalDateTime.of(2026, 5, 11, 10, 0));
    private final Date field = date(LocalDateTime.of(2026, 5, 11, 10, 0));

    private static Date staticMethod() {
        return date(LocalDateTime.of(2026, 5, 11, 10, 0));
    }

    private Date method() {
        return date(LocalDateTime.of(2026, 5, 11, 10, 0));
    }

    @TestTemplate
    public void testDateEquals(TestDatabase testDb) throws SQLException {
        var date = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime='2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().equals(date))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateAfter(TestDatabase testDb) throws SQLException {
        var date = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime>'2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().after(date))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateBefore(TestDatabase testDb) throws SQLException {
        var date = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime<'2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().before(date))
                .list(), Appointment.class
        );
    }

    // The same comparisons with a java.sql.Timestamp operand, which every JDBC driver accepts in
    // setObject(). If the java.util.Date tests above fail on a database and these pass, the
    // problem is how the parameter is bound, not how the query is decoded.

    @TestTemplate
    public void testDateEqualsTimestamp(TestDatabase testDb) throws SQLException {
        var date = Timestamp.valueOf(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime='2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().equals(date))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateAfterTimestamp(TestDatabase testDb) throws SQLException {
        var date = Timestamp.valueOf(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime>'2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().after(date))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateBeforeTimestamp(TestDatabase testDb) throws SQLException {
        var date = Timestamp.valueOf(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime<'2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().before(date))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateIsNull(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime IS NULL", from(testDb, Appointment.class)
                .where(a -> a.startTime() == null)
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateIsNotNull(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime IS NOT NULL", from(testDb, Appointment.class)
                .where(a -> a.startTime() != null)
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateEqualsField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime='2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().equals(field))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateEqualsStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime='2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().equals(staticField))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateEqualsMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime='2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().equals(method()))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateEqualsStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime='2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().equals(staticMethod()))
                .list(), Appointment.class
        );
    }

    // Negated and combined comparisons. These reach Flip, which has to invert before/after rather
    // than return them unchanged.

    @TestTemplate
    public void testDateNotAfter(TestDatabase testDb) throws SQLException {
        var date = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime<='2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> !a.startTime().after(date))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateNotBefore(TestDatabase testDb) throws SQLException {
        var date = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime>='2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> !a.startTime().before(date))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateNotEquals(TestDatabase testDb) throws SQLException {
        var date = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime<>'2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> !a.startTime().equals(date))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateAfterAndBefore(TestDatabase testDb) throws SQLException {
        var from = date(LocalDateTime.of(2026, 5, 11, 9, 0));
        var to = date(LocalDateTime.of(2026, 5, 11, 11, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime>'2026-05-11 09:00' AND startTime<'2026-05-11 11:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().after(from) && a.startTime().before(to))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateBeforeOrAfter(TestDatabase testDb) throws SQLException {
        var from = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        var to = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime<'2026-05-11 10:00' OR startTime>'2026-05-11 10:00'", from(testDb, Appointment.class)
                .where(a -> a.startTime().before(from) || a.startTime().after(to))
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateNotAfterAndId(TestDatabase testDb) throws SQLException {
        var date = date(LocalDateTime.of(2026, 5, 11, 10, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime<='2026-05-11 10:00' AND id>1", from(testDb, Appointment.class)
                .where(a -> !a.startTime().after(date) && a.id() > 1)
                .list(), Appointment.class
        );
    }

    @TestTemplate
    public void testDateNotBeforeOrId(TestDatabase testDb) throws SQLException {
        var date = date(LocalDateTime.of(2026, 5, 11, 11, 0));
        testListOfRecord(testDb, "SELECT * FROM Appointment WHERE startTime>='2026-05-11 11:00' OR id=1", from(testDb, Appointment.class)
                .where(a -> !a.startTime().before(date) || a.id() == 1)
                .list(), Appointment.class
        );
    }

}
