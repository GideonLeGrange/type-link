package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Reading;

import java.sql.SQLException;

/**
 * Ordering by every primitive component type. Each type compiles to a different return opcode in the accessor, and
 * the lambda parser has to handle all of them.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0410_PrimitiveOrderBy extends DatabaseTest {

    @TestTemplate
    public void testOrderByLong(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY bigNum", from(testDb, Reading.class)
                .orderBy(Reading::bigNum)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByLongLambda(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY bigNum", from(testDb, Reading.class)
                .orderBy(r -> r.bigNum())
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByLongDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY bigNum DESC", from(testDb, Reading.class)
                .orderByDescending(Reading::bigNum)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByFloat(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY ratio", from(testDb, Reading.class)
                .orderBy(Reading::ratio)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByFloatDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY ratio DESC", from(testDb, Reading.class)
                .orderByDescending(Reading::ratio)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByShort(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY small", from(testDb, Reading.class)
                .orderBy(Reading::small)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByShortDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY small DESC", from(testDb, Reading.class)
                .orderByDescending(Reading::small)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByByte(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY tiny", from(testDb, Reading.class)
                .orderBy(Reading::tiny)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByByteDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY tiny DESC", from(testDb, Reading.class)
                .orderByDescending(Reading::tiny)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByChar(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY grade", from(testDb, Reading.class)
                .orderBy(Reading::grade)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByCharDescending(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY grade DESC", from(testDb, Reading.class)
                .orderByDescending(Reading::grade)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByBooleanThenByLong(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY flag, bigNum", from(testDb, Reading.class)
                .orderBy(Reading::flag)
                .thenBy(Reading::bigNum)
                .list(), Reading.class);
    }

    @TestTemplate
    public void testOrderByThenByEveryPrimitive(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading ORDER BY flag DESC, ratio, small DESC, tiny, grade DESC, bigNum",
                from(testDb, Reading.class)
                        .orderByDescending(Reading::flag)
                        .thenBy(Reading::ratio)
                        .thenByDescending(Reading::small)
                        .thenBy(Reading::tiny)
                        .thenByDescending(Reading::grade)
                        .thenBy(Reading::bigNum)
                        .list(), Reading.class);
    }

    @TestTemplate
    public void testWhereOnLongAndOrderByLong(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Reading WHERE bigNum > 4000000000 ORDER BY bigNum",
                from(testDb, Reading.class)
                        .where(r -> r.bigNum() > 4_000_000_000L)
                        .orderBy(Reading::bigNum)
                        .list(), Reading.class);
    }

}
