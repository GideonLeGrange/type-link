package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Gauge;
import rec.Meter;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A boxed type says "this column may be NULL", so a NULL must come back as null. {@code ResultSet.getInt} and its
 * relatives answer 0 (or false) for a NULL, which is right for a primitive and wrong for a boxed type.
 * <p>
 * Row 1 holds a value in every column, row 2 holds NULL in every column but the key.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0900_NullableColumns extends DatabaseTest {

    private static void createData(TestDatabase testDb) throws SQLException {
        try (var con = testDb.getConnection(); var stmt = con.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Gauge");
            stmt.execute("CREATE TABLE Gauge (id BIGINT PRIMARY KEY, count INT NULL, total BIGINT NULL,"
                    + " ratio DOUBLE PRECISION NULL, flag BOOLEAN NULL, small SMALLINT NULL, weight REAL NULL,"
                    + " tiny SMALLINT NULL, grade CHAR(1) NULL, plainCount INT NULL)");
            stmt.execute("INSERT INTO Gauge VALUES (1, 5, 6000000000, 2.5, true, 7, 1.5, 3, 'A', 9)");
            stmt.execute("INSERT INTO Gauge (id) VALUES (2)");
            stmt.execute("DROP TABLE IF EXISTS Meter");
            stmt.execute("CREATE TABLE Meter (id BIGINT PRIMARY KEY, gaugeId BIGINT NOT NULL)");
            stmt.execute("INSERT INTO Meter VALUES (1, 1), (2, 99)");
        }
    }

    private Gauge row(TestDatabase testDb, long id) throws SQLException {
        return from(testDb, Gauge.class).where(g -> g.id() == id).list().getFirst();
    }

    @TestTemplate
    public void boxedColumnsKeepTheirValues(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var g = row(testDb, 1);

        assertThat(g.count()).isEqualTo(5);
        assertThat(g.total()).isEqualTo(6_000_000_000L);
        assertThat(g.ratio()).isEqualTo(2.5);
        assertThat(g.flag()).isTrue();
        assertThat(g.small()).isEqualTo((short) 7);
        assertThat(g.weight()).isEqualTo(1.5f);
        assertThat(g.tiny()).isEqualTo((byte) 3);
        assertThat(g.grade()).isEqualTo('A');
        assertThat(g.plainCount()).isEqualTo(9);
    }

    @TestTemplate
    public void aNullInABoxedColumnIsNull(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var g = row(testDb, 2);

        assertThat(g.count()).isNull();
        assertThat(g.total()).isNull();
        assertThat(g.ratio()).isNull();
        assertThat(g.flag()).isNull();
        assertThat(g.small()).isNull();
        assertThat(g.weight()).isNull();
        assertThat(g.tiny()).isNull();
        assertThat(g.grade()).isNull();
    }

    /** A primitive has no null to return, so it reads as zero, as it always has. */
    @TestTemplate
    public void aNullInAPrimitiveColumnStillReadsAsZero(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(row(testDb, 2).plainCount()).isZero();
    }

    @TestTemplate
    public void aBoxedColumnProjectedOnItsOwnKeepsNull(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var counts = from(testDb, Gauge.class).orderBy(g -> g.id()).list(g -> g.count());
        var flags = from(testDb, Gauge.class).orderBy(g -> g.id()).list(g -> g.flag());

        assertThat(counts).containsExactly(5, null);
        assertThat(flags).containsExactly(true, null);
    }

    @TestTemplate
    public void aRowWithNoMatchInALeftJoinHasNullBoxedColumns(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var counts = from(testDb, Meter.class)
                .leftJoin(Gauge.class, (m, g) -> m.gaugeId() == g.id())
                .orderBy((m, g) -> m.id())
                .list((m, g) -> g.count());

        // meter 1 points at gauge 1, which has a count; meter 2 points at no gauge
        assertThat(counts).containsExactly(5, null);
    }

    /** Aggregates are computed, not read from a column: an empty sum is still a number. */
    @TestTemplate
    public void computedValuesAreReadAsBefore(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(from(testDb, Gauge.class).where(g -> g.id() > 100).count()).isZero();
        int sum = from(testDb, Gauge.class).sum(g -> g.plainCount());
        assertThat(sum).isEqualTo(9);
    }
}
