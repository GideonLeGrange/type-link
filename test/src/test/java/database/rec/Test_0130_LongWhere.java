package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Town;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0130_LongWhere extends DatabaseTest {

    private static final long staticField = 10L;
    private static final Long staticObjectField = 10L;
    private final long field = 10L;
    private final Long objectField = 10L;

    private static Long staticObjectMethod() {
        return 10L;
    }

    private static long staticMethod() {
        return 10L;
    }

    private long method() {
        return 10L;
    }

    private Long objectMethod() {
        return 10L;
    }

    @TestTemplate
    public void testLongEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id() == 10)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongIn(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id IN (9,10)", from(testDb, Town.class)
                .where(p -> List.of(9L,10L).contains(p.id()))
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongLess(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id<10", from(testDb, Town.class)
                .where(p -> p.id() < 10)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongGreater(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id>10", from(testDb, Town.class)
                .where(p -> p.id() > 10)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongNotEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id<>10", from(testDb, Town.class)
                .where(p -> p.id() != 10)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongLessOrEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id<=10", from(testDb, Town.class)
                .where(p -> p.id() <= 10)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongGreaterOrEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id>=10", from(testDb, Town.class)
                .where(p -> p.id() >= 10)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongEqualsField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id() == field)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongEqualsObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id().equals(objectField))
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongEqualsStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id() == staticField)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongEqualsStaticObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id().equals(staticObjectField))
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongEqualsMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id() == method())
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongEqualsObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id().equals(objectMethod()))
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongEqualsStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id().equals(staticMethod()))
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testLongEqualsStaticObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE id=10", from(testDb, Town.class)
                .where(p -> p.id().equals(staticObjectMethod()))
                .list(), Town.class
        );
    }
}
