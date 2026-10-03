package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Reading;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

import static me.legrange.typelink.Selects.max;
import static me.legrange.typelink.Selects.min;
import static me.legrange.typelink.Selects.sum;

/** Primitive components of types other than int, double and boolean in projections, group by and aggregates. */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_1010_PrimitiveGroupByAndProjection extends DatabaseTest {

    @TestTemplate
    public void testProjectLong(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT bigNum FROM Reading ORDER BY id", Long.class);
        var have = from(testDb, Reading.class)
                .orderBy(Reading::id)
                .list(Reading::bigNum);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testProjectFloat(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT ratio FROM Reading ORDER BY id", Float.class);
        var have = from(testDb, Reading.class)
                .orderBy(Reading::id)
                .list(Reading::ratio);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testProjectShort(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT small FROM Reading ORDER BY id", Short.class);
        var have = from(testDb, Reading.class)
                .orderBy(Reading::id)
                .list(Reading::small);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testProjectByte(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT tiny FROM Reading ORDER BY id", Byte.class);
        var have = from(testDb, Reading.class)
                .orderBy(Reading::id)
                .list(Reading::tiny);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testProjectChar(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT grade FROM Reading ORDER BY id", Character.class);
        var have = from(testDb, Reading.class)
                .orderBy(Reading::id)
                .list(Reading::grade);
        assertExpected(want, have);
        assertThat(have).allMatch(Character.class::isInstance);
    }

    @TestTemplate
    public void testProjectBoolean(TestDatabase testDb) throws SQLException {
        var want = select(testDb, "SELECT flag FROM Reading ORDER BY id", Boolean.class);
        var have = from(testDb, Reading.class)
                .orderBy(Reading::id)
                .list(Reading::flag);
        assertExpected(want, have);
    }

    @TestTemplate
    public void testProjectAllPrimitives(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT bigNum,ratio,small FROM Reading ORDER BY bigNum", from(testDb, Reading.class)
                .orderBy(Reading::bigNum)
                .list(Reading::bigNum, Reading::ratio, Reading::small),
                Long.class, Float.class, Short.class);
    }

    @TestTemplate
    public void testGroupByBooleanWithLongAggregates(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb,
                "SELECT flag,SUM(bigNum),MAX(bigNum) FROM Reading GROUP BY flag ORDER BY flag",
                from(testDb, Reading.class)
                        .groupBy(Reading::flag)
                        .orderBy(Reading::flag)
                        .list(Reading::flag, r -> sum(r.bigNum()), r -> max(r.bigNum())),
                Boolean.class, Long.class, Long.class);
    }

    @TestTemplate
    public void testGroupByLong(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT bigNum,MAX(ratio) FROM Reading GROUP BY bigNum ORDER BY bigNum",
                from(testDb, Reading.class)
                        .groupBy(Reading::bigNum)
                        .orderBy(Reading::bigNum)
                        .list(Reading::bigNum, r -> max(r.ratio())),
                Long.class, Float.class);
    }

    @TestTemplate
    public void testGroupByFloat(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT ratio,MIN(bigNum) FROM Reading GROUP BY ratio ORDER BY ratio",
                from(testDb, Reading.class)
                        .groupBy(Reading::ratio)
                        .orderBy(Reading::ratio)
                        .list(Reading::ratio, r -> min(r.bigNum())),
                Float.class, Long.class);
    }

    @TestTemplate
    public void testGroupByShort(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT small,MAX(bigNum) FROM Reading GROUP BY small ORDER BY small",
                from(testDb, Reading.class)
                        .groupBy(Reading::small)
                        .orderBy(Reading::small)
                        .list(Reading::small, r -> max(r.bigNum())),
                Short.class, Long.class);
    }

    @TestTemplate
    public void testGroupByByte(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT tiny,MAX(bigNum) FROM Reading GROUP BY tiny ORDER BY tiny",
                from(testDb, Reading.class)
                        .groupBy(Reading::tiny)
                        .orderBy(Reading::tiny)
                        .list(Reading::tiny, r -> max(r.bigNum())),
                Byte.class, Long.class);
    }

    @TestTemplate
    public void testGroupByChar(TestDatabase testDb) throws SQLException {
        testListOfRow(testDb, "SELECT grade,MAX(bigNum) FROM Reading GROUP BY grade ORDER BY grade",
                from(testDb, Reading.class)
                        .groupBy(Reading::grade)
                        .orderBy(Reading::grade)
                        .list(Reading::grade, r -> max(r.bigNum())),
                Character.class, Long.class);
    }

}
