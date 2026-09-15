package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Invoice;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0140_DoubleWhere extends DatabaseTest {

    private static final double staticField = 100D;
    private static final Double staticObjectField = 100D;
    private final double field = 100D;
    private final Double objectField = 100D;

    private static Double staticObjectMethod() {
        return 100D;
    }

    private static double staticMethod() {
        return 100D;
    }

    private double method() {
        return 100D;
    }

    private Double objectMethod() {
        return 100D;
    }

    @TestTemplate
    public void testDoubleEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount=100", from(testDb, Invoice.class)
                .where(p -> p.amount() == 100)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleLess(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount<100", from(testDb, Invoice.class)
                .where(p -> p.amount() < 100)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleGreater(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>100", from(testDb, Invoice.class)
                .where(p -> p.amount() > 100)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleNotEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount<>100", from(testDb, Invoice.class)
                .where(p -> p.amount() != 100)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleLessOrEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount<=100", from(testDb, Invoice.class)
                .where(p -> p.amount() <= 100)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleGreaterOrEquals(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= 100)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= field)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleStaticField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= staticField)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleStaticObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= staticObjectField)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= method())
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleStaticMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= staticMethod())
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleObjectField(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= objectField)
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= objectMethod())
                .list(), Invoice.class
        );
    }

    @TestTemplate
    public void testDoubleStaticObjectMethod(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice WHERE amount>=100", from(testDb, Invoice.class)
                .where(p -> p.amount() >= staticObjectMethod())
                .list(), Invoice.class
        );
    }

}
