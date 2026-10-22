package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Schedule;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Instant} has no SQL type of its own - it is bound and read back as a {@code TIMESTAMP}, the
 * same as {@code java.util.Date} - so rows are inserted through a bound {@link Timestamp} parameter
 * rather than a literal SQL string, to keep this test independent of how the JVM's default time zone
 * would otherwise affect parsing that literal.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0183_InstantWhere extends DatabaseTest {

    private static final Instant EARLY = Instant.parse("2026-05-11T09:00:00Z");
    private static final Instant LATE = Instant.parse("2026-05-14T10:00:00Z");

    private static void createData(TestDatabase testDb) throws SQLException {
        try (var con = testDb.getConnection(); var stmt = con.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Schedule");
            stmt.execute("CREATE TABLE Schedule (id BIGINT PRIMARY KEY, startsAt TIMESTAMP NULL)");
        }
        try (var con = testDb.getConnection();
             var stmt = con.prepareStatement("INSERT INTO Schedule VALUES (?, ?)")) {
            stmt.setLong(1, 1L);
            stmt.setObject(2, Timestamp.from(EARLY));
            stmt.executeUpdate();
            stmt.setLong(1, 2L);
            stmt.setObject(2, Timestamp.from(LATE));
            stmt.executeUpdate();
            stmt.setLong(1, 3L);
            stmt.setNull(2, java.sql.Types.TIMESTAMP);
            stmt.executeUpdate();
        }
    }

    private List<Long> ids(List<Schedule> rows) {
        return rows.stream().map(Schedule::id).sorted().toList();
    }

    @TestTemplate
    public void instantEquals(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(ids(from(testDb, Schedule.class).where(s -> s.startsAt().equals(EARLY)).list())).containsExactly(1L);
    }

    @TestTemplate
    public void instantIsAfter(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(ids(from(testDb, Schedule.class).where(s -> s.startsAt().isAfter(EARLY)).list())).containsExactly(2L);
    }

    @TestTemplate
    public void instantIsBefore(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(ids(from(testDb, Schedule.class).where(s -> s.startsAt().isBefore(LATE)).list())).containsExactly(1L);
    }

    @TestTemplate
    public void instantIsNull(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(ids(from(testDb, Schedule.class).where(s -> s.startsAt() == null).list())).containsExactly(3L);
    }

    @TestTemplate
    public void instantIsNotNull(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(ids(from(testDb, Schedule.class).where(s -> s.startsAt() != null).list())).containsExactly(1L, 2L);
    }

    /** Negated comparisons reach Flip, which has to invert isAfter/isBefore rather than return them unchanged. */
    @TestTemplate
    public void instantNotIsAfter(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(ids(from(testDb, Schedule.class).where(s -> !s.startsAt().isAfter(EARLY)).list())).containsExactly(1L);
    }

    @TestTemplate
    public void instantRoundTripsExactly(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(from(testDb, Schedule.class).where(s -> s.id() == 1L).list().getFirst().startsAt()).isEqualTo(EARLY);
    }
}
