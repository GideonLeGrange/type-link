package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Host;
import rec.Nic;

import java.sql.SQLException;

/**
 * An AND group followed by an OR must keep its meaning under SQL's three-valued logic.
 * <p>
 * {@code (A && X) || B} is generated as {@code (NOT A AND B) OR (A AND (X OR B))}. That is equivalent when every
 * value is TRUE or FALSE, but with a LEFT JOIN {@code A} is NULL for a row with no match, and the expanded form is
 * then NULL instead of TRUE when {@code B} is true, so the row is dropped.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0313_AndGroupThenOrWithNulls extends DatabaseTest {

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

    /**
     * Host 3 has no NIC, so its NIC columns are NULL; it matches only through its name.
     */
    @TestTemplate
    public void testAndGroupThenOrAcrossALeftJoin(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT Host.* FROM Host LEFT OUTER JOIN Nic ON Host.id = Nic.hostId"
                        + " WHERE (Nic.address LIKE '%10.0%' AND Nic.walled=false) OR Host.name LIKE '%gam%'",
                from(testDb, Host.class)
                        .leftJoin(Nic.class, (h, n) -> h.id() == n.hostId())
                        .where((h, n) -> (n.address().contains("10.0") && !n.walled()) || h.name().contains("gam"))
                        .list((h, _) -> h), Host.class);
    }

    /**
     * The same, without a boolean test: isolates the NULL handling from the lost negation.
     */
    @TestTemplate
    public void testAndGroupWithoutBooleanThenOrAcrossALeftJoin(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT Host.* FROM Host LEFT OUTER JOIN Nic ON Host.id = Nic.hostId"
                        + " WHERE (Nic.address LIKE '%10.0%' AND Nic.id > 0) OR Host.name LIKE '%gam%'",
                from(testDb, Host.class)
                        .leftJoin(Nic.class, (h, n) -> h.id() == n.hostId())
                        .where((h, n) -> (n.address().contains("10.0") && n.id() > 0) || h.name().contains("gam"))
                        .list((h, _) -> h), Host.class);
    }

    /**
     * Three terms in the AND group: (A AND B AND C) OR D.
     */
    @TestTemplate
    public void testLongerAndGroupThenOrAcrossALeftJoin(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT Host.* FROM Host LEFT OUTER JOIN Nic ON Host.id = Nic.hostId"
                        + " WHERE (Nic.address LIKE '%10.0%' AND Nic.id > 0 AND Nic.id < 9) OR Host.name LIKE '%gam%'",
                from(testDb, Host.class)
                        .leftJoin(Nic.class, (h, n) -> h.id() == n.hostId())
                        .where((h, n) -> (n.address().contains("10.0") && n.id() > 0 && n.id() < 9) || h.name().contains("gam"))
                        .list((h, _) -> h), Host.class);
    }

    /**
     * The dual shape: (A OR B) AND C, where A is NULL for a host with no NIC.
     */
    @TestTemplate
    public void testOrGroupThenAndAcrossALeftJoin(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT Host.* FROM Host LEFT OUTER JOIN Nic ON Host.id = Nic.hostId"
                        + " WHERE (Nic.address LIKE '%10.0%' OR Host.name LIKE '%gam%') AND Host.id > 0",
                from(testDb, Host.class)
                        .leftJoin(Nic.class, (h, n) -> h.id() == n.hostId())
                        .where((h, n) -> (n.address().contains("10.0") || h.name().contains("gam")) && h.id() > 0)
                        .list((h, _) -> h), Host.class);
    }

    /**
     * Both operands of the OR are AND groups: (A AND B) OR (C AND D).
     */
    @TestTemplate
    public void testTwoAndGroupsAcrossALeftJoin(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT Host.* FROM Host LEFT OUTER JOIN Nic ON Host.id = Nic.hostId"
                        + " WHERE (Nic.address LIKE '%10.0%' AND Nic.id > 0) OR (Host.name LIKE '%gam%' AND Host.id > 0)",
                from(testDb, Host.class)
                        .leftJoin(Nic.class, (h, n) -> h.id() == n.hostId())
                        .where((h, n) -> (n.address().contains("10.0") && n.id() > 0) || (h.name().contains("gam") && h.id() > 0))
                        .list((h, _) -> h), Host.class);
    }

    /**
     * An OR first, then an AND group: B OR (A AND C). Generated correctly today; guards against regressions.
     */
    @TestTemplate
    public void testOrThenAndGroupAcrossALeftJoin(TestDatabase testDb) throws SQLException {
        createData(testDb);
        testListOfRecord(testDb, "SELECT Host.* FROM Host LEFT OUTER JOIN Nic ON Host.id = Nic.hostId"
                        + " WHERE Host.name LIKE '%gam%' OR (Nic.address LIKE '%10.0%' AND Nic.id > 0)",
                from(testDb, Host.class)
                        .leftJoin(Nic.class, (h, n) -> h.id() == n.hostId())
                        .where((h, n) -> h.name().contains("gam") || (n.address().contains("10.0") && n.id() > 0))
                        .list((h, _) -> h), Host.class);
    }
}
