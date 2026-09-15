package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Invoice;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0810_Where1Limit extends DatabaseTest {

    @TestTemplate
    public void testWhere1Limit(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                "SELECT * FROM Invoice WHERE amount > 100 LIMIT 3",
                from(testDb, Invoice.class)
                        .where(i -> i.amount() > 100)
                        .limit(3)
                        .list(),
                Invoice.class);
    }

    @TestTemplate
    public void testWhere1LimitWithOffset(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb,
                "SELECT * FROM Invoice WHERE amount > 100 LIMIT 3 OFFSET 2",
                from(testDb, Invoice.class)
                        .where(i -> i.amount() > 100)
                        .limit(3, 2)
                        .list(),
                Invoice.class);
    }

}

