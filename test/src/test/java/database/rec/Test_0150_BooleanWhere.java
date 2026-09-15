package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Invoice;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0150_BooleanWhere extends DatabaseTest {

    private static final boolean staticField = true;
    private static final Boolean staticObjectField = true;
    private final boolean field = true;
    private final Boolean objectField = true;

    private static Boolean staticObjectMethod() {
        return true;
    }

    private static boolean staticMethod() {
        return true;
    }

    private boolean method() {
        return true;
    }

    private Boolean objectMethod() {
        return true;
    }

    @TestTemplate
    public void testBooleanEquals(TestDatabase testDb) throws SQLException {
        //noinspection PointlessBooleanExpression
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == true)
                .list(), Invoice.class
        );
    }


    @TestTemplate
    public void testBooleanNotEquals(TestDatabase testDb) throws SQLException {
        //noinspection PointlessBooleanExpression
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid<>true", from(testDb, Invoice.class)
                .where(p -> p.paid() == false)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsMethodCall(TestDatabase testDb) throws SQLException {
        //noinspection Convert2MethodRef
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid())
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanNotEqualsMethodCall(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid<>true", from(testDb, Invoice.class)
                .where(p -> !p.paid())
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsMethodRef(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(Invoice::paid)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == field)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == objectField)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == staticField)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsStaticObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == staticObjectField)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == method())
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == objectMethod())
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == staticMethod())
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testBooleanEqualsStaticObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE paid=true", from(testDb, Invoice.class)
                .where(p -> p.paid() == staticObjectMethod())
                .list(), Invoice.class
        );
    }

}
