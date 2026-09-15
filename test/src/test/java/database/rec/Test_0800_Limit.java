package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import rec.Invoice;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

@SuppressWarnings("NewClassNamingConvention")
public final class Test_0800_Limit extends DatabaseTest {

    @TestTemplate
    public void testLimit1(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice LIMIT 2", from(testDb, Invoice.class)
                .limit(2)
                .list(), Invoice.class);
    }

    @TestTemplate
    public void testLimitWithOffset(TestDatabase testDb) throws SQLException {
        testListOfRecord(testDb, "SELECT * FROM Invoice ORDER BY clientId,id LIMIT 2 OFFSET 2", from(testDb, Invoice.class)
                .orderBy(Invoice::clientId)
                .thenBy(Invoice::id)
                .limit(2, 2)
                .list(), Invoice.class
        );
    }

}
