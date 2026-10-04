package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Host;
import rec.Nic;

import java.sql.SQLException;

/**
 * An AND group holding a boolean test, followed by an OR, must mean what the source says.
 * <p>
 * {@code (A && !n.walled()) || B} is generated as {@code (NOT A AND B) OR (A AND (walled = ? OR B))}: the
 * restructuring is equivalent in two-valued logic, but the bound value for {@code walled} is {@code true} where the
 * source asked for {@code false}, so the negation is lost. The same condition written {@code B || (A && !n.walled())}
 * is generated correctly.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0312_BooleanNegationInOr extends DatabaseTest {

    /**
     * Hosts 1 (alpha), 2 (beta), 3 (gamma, no NIC) and 4 (delta); NICs: 1 and 3 are not walled, 2 is walled.
     */
    private static void createData(TestDatabase testDb) throws SQLException {
        try (var con = testDb.getConnection(); var stmt = con.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Nic");
            stmt.execute("DROP TABLE IF EXISTS Host");
            stmt.execute("CREATE TABLE Host (id BIGINT PRIMARY KEY, name VARCHAR(32))");
            stmt.execute("CREATE TABLE Nic (id BIGINT PRIMARY KEY, hostId BIGINT, address VARCHAR(32), walled BOOLEAN)");
            stmt.execute("INSERT INTO Host VALUES (1, 'alpha'), (2, 'beta'), (3, 'gamma'), (4, 'delta')");
            stmt.execute("INSERT INTO Nic VALUES (1, 1, '10.0.0.1', false), (2, 2, '10.0.0.2', true), (3, 4, '192.168.1.1', false)");
        }
    }

    @TestTemplate
    public void testAndNotBooleanThenOr(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT * FROM Nic WHERE (address LIKE '%10.0%' AND walled=false) OR address LIKE '%192%'",
                from(testDb, Nic.class)
                        .where(n -> (n.address().contains("10.0") && !n.walled()) || n.address().contains("192"))
                        .list(), Nic.class);
    }

    @TestTemplate
    public void testAndNotBooleanThenOrWithoutParentheses(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT * FROM Nic WHERE (address LIKE '%10.0%' AND walled=false) OR address LIKE '%192%'",
                from(testDb, Nic.class)
                        .where(n -> n.address().contains("10.0") && !n.walled() || n.address().contains("192"))
                        .list(), Nic.class);
    }

    @TestTemplate
    public void testAndBooleanThenOr(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT * FROM Nic WHERE (address LIKE '%10.0%' AND walled=true) OR address LIKE '%192%'",
                from(testDb, Nic.class)
                        .where(n -> (n.address().contains("10.0") && n.walled()) || n.address().contains("192"))
                        .list(), Nic.class);
    }

    @TestTemplate
    public void testOrThenAndNotBoolean(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT * FROM Nic WHERE address LIKE '%192%' OR (address LIKE '%10.0%' AND walled=false)",
                from(testDb, Nic.class)
                        .where(n -> n.address().contains("192") || (n.address().contains("10.0") && !n.walled()))
                        .list(), Nic.class);
    }

    @TestTemplate
    public void testTwoAndGroupsWithBooleans(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT * FROM Nic WHERE (address LIKE '%10.0%' AND walled=false) OR (address LIKE '%192%' AND walled=false)",
                from(testDb, Nic.class)
                        .where(n -> (n.address().contains("10.0") && !n.walled()) || (n.address().contains("192") && !n.walled()))
                        .list(), Nic.class);
    }

    @TestTemplate
    public void testAndNotBooleanThenOrThenOr(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT * FROM Nic WHERE (address LIKE '%10.0%' AND walled=false) OR address LIKE '%192%' OR address LIKE '%zzz%'",
                from(testDb, Nic.class)
                        .where(n -> (n.address().contains("10.0") && !n.walled()) || n.address().contains("192") || n.address().contains("zzz"))
                        .list(), Nic.class);
    }
}
