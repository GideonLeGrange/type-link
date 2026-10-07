package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Ledger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Time of day, {@code BigDecimal}, {@code BigInteger}, {@code short}, {@code byte} and {@code char} columns: read,
 * and compared in a condition.
 * <p>
 * Row 1 opens at 08:30 with amount 10.00; row 2 opens at 17:00 with amount 99.50.
 */
@SuppressWarnings({"NewClassNamingConvention", "Convert2MethodRef"})
public final class Test_0910_ExactAndTimeTypes extends DatabaseTest {

    private static final BigInteger BIG = new BigInteger("12345678901234567890");

    private static void createData(TestDatabase testDb) throws SQLException {
        try (var con = testDb.getConnection(); var stmt = con.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Ledger");
            stmt.execute("CREATE TABLE Ledger (id BIGINT PRIMARY KEY, opens TIME, amount DECIMAL(10,2),"
                    + " serial DECIMAL(30,0), copies SMALLINT, level SMALLINT, initial CHAR(1), grade CHAR(1) NULL)");
            stmt.execute("INSERT INTO Ledger VALUES (1, '08:30:00', 10.00, 12345678901234567890, 3, 7, 'Q', 'A')");
            stmt.execute("INSERT INTO Ledger VALUES (2, '17:00:00', 99.50, 5, 4, 8, 'R', NULL)");
        }
    }

    private List<Long> ids(List<Ledger> rows) {
        return rows.stream().map(Ledger::id).sorted().toList();
    }

    @TestTemplate
    public void theTypesAreReadAsThemselves(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var row = from(testDb, Ledger.class).where(l -> l.id() == 1L).list().getFirst();

        assertThat(row.opens()).isEqualTo(LocalTime.of(8, 30));
        assertThat(row.amount()).isEqualByComparingTo("10.00");
        assertThat(row.serial()).isEqualTo(BIG);
        assertThat(row.copies()).isEqualTo((short) 3);
        assertThat(row.level()).isEqualTo((byte) 7);
        assertThat(row.initial()).isEqualTo('Q');
        assertThat(row.grade()).isEqualTo('A');
    }

    @TestTemplate
    public void aNullCharacterColumnIsNull(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(from(testDb, Ledger.class).where(l -> l.id() == 2L).list().getFirst().grade()).isNull();
    }

    // --- time of day ---------------------------------------------------------

    @TestTemplate
    public void timeOfDayEquals(TestDatabase testDb) throws SQLException {
        createData(testDb);
        final var half = LocalTime.of(8, 30);

        assertThat(ids(from(testDb, Ledger.class).where(l -> l.opens().equals(half)).list())).containsExactly(1L);
    }

    @TestTemplate
    public void timeOfDayBeforeAndAfter(TestDatabase testDb) throws SQLException {
        createData(testDb);
        final var noon = LocalTime.of(12, 0);

        assertThat(ids(from(testDb, Ledger.class).where(l -> l.opens().isBefore(noon)).list())).containsExactly(1L);
        assertThat(ids(from(testDb, Ledger.class).where(l -> l.opens().isAfter(noon)).list())).containsExactly(2L);
    }

    // --- exact numbers -------------------------------------------------------

    @TestTemplate
    public void bigIntegerEquals(TestDatabase testDb) throws SQLException {
        createData(testDb);
        final var serial = BIG;

        assertThat(ids(from(testDb, Ledger.class).where(l -> l.serial().equals(serial)).list())).containsExactly(1L);
    }

    @TestTemplate
    public void bigDecimalEqualsByValueNotByScale(TestDatabase testDb) throws SQLException {
        createData(testDb);
        // BigDecimal.equals in Java says 10.0 and 10.00 differ; the database compares the numbers
        final var amount = new BigDecimal("10.0");

        assertThat(ids(from(testDb, Ledger.class).where(l -> l.amount().equals(amount)).list())).containsExactly(1L);
    }

    @TestTemplate
    public void exactNumbersInASet(TestDatabase testDb) throws SQLException {
        createData(testDb);
        final var amounts = Set.of(new BigDecimal("99.50"), new BigDecimal("1.00"));
        final var serials = Set.of(BigInteger.valueOf(5), BigInteger.TEN);

        assertThat(ids(from(testDb, Ledger.class).where(l -> amounts.contains(l.amount())).list())).containsExactly(2L);
        assertThat(ids(from(testDb, Ledger.class).where(l -> serials.contains(l.serial())).list())).containsExactly(2L);
    }

    // --- small integers and characters ---------------------------------------

    @TestTemplate
    public void shortAndByteCompare(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(ids(from(testDb, Ledger.class).where(l -> l.copies() == 3).list())).containsExactly(1L);
        assertThat(ids(from(testDb, Ledger.class).where(l -> l.level() > 7).list())).containsExactly(2L);
    }

    @TestTemplate
    public void aCharCompares(TestDatabase testDb) throws SQLException {
        createData(testDb);

        assertThat(ids(from(testDb, Ledger.class).where(l -> l.initial() == 'Q').list())).containsExactly(1L);
        assertThat(ids(from(testDb, Ledger.class).where(l -> l.initial() != 'Q').list())).containsExactly(2L);
    }

    @TestTemplate
    public void charactersInASet(TestDatabase testDb) throws SQLException {
        createData(testDb);
        final var initials = Set.of('R', 'Z');

        assertThat(ids(from(testDb, Ledger.class).where(l -> initials.contains(l.initial())).list())).containsExactly(2L);
    }
}
