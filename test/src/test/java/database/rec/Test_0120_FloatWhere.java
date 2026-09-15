package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Town;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.util.Set;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0120_FloatWhere extends DatabaseTest {

    private static final float staticField = 100F;
    private static final Float staticObjectField = 100F;
    private final float field = 100F;
    private final Float objectField = 100F;

    private static Float staticObjectMethod() {
        return 100F;
    }

    private static float staticMethod() {
        return 100F;
    }

    private float method() {
        return 100F;
    }

    private Float objectMethod() {
        return 100F;
    }

    @TestTemplate
    public void testFloatEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == 100)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatInConst(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt IN(195,100)", from(testDb, Town.class)
                .where(t -> Set.of(195F, 100F).contains(t.alt()))
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatLess(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt<100", from(testDb, Town.class)
                .where(p -> p.alt() < 100)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatGreater(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt>100", from(testDb, Town.class)
                .where(p -> p.alt() > 100)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatNotEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt<>100", from(testDb, Town.class)
                .where(p -> p.alt() != 100)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatLessOrEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt<=100", from(testDb, Town.class)
                .where(p -> p.alt() <= 100)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatGreaterOrEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt>=100", from(testDb, Town.class)
                .where(p -> p.alt() >= 100)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatEqualsPrimitiveMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == method())
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatEqualsPrimitiveField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == field)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatEqualsPrimitiveStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == staticField)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatEqualsObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == objectField)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatEqualsObjectStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == staticObjectField)
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatEqualsPrimitiveStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == staticMethod())
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatEqualsObjectStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == staticObjectMethod())
                .list(), Town.class
        );
    }

    @TestTemplate
    public void testFloatEqualsObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Town WHERE alt=100", from(testDb, Town.class)
                .where(p -> p.alt() == objectMethod())
                .list(), Town.class
        );
    }


}
