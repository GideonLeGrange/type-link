package optional;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import me.legrange.typelink.Database;
import me.legrange.typelink.builder.DatabaseBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Experimental. What happens when an {@code Optional} getter is projected rather than filtered on.
 * <p>
 * Two different problems:
 * <ul>
 *   <li>{@code Optional} calls in a projection fail to parse, like they do in a filter.</li>
 *   <li>Projecting the bare getter parses, but returns the raw column value (a {@code LocalDate} or null) in a list
 *       typed {@code List<Optional<LocalDate>>}. That is silent: nothing fails until the caller uses an element.</li>
 * </ul>
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_7010_OptionalGetterProjections extends DatabaseTest {

    private static Database<Record> database(TestDatabase testDb) {
        return DatabaseBuilder.of(Record.class)
                .connection(() -> {
                    try {
                        return testDb.getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .mapper(new OptionalMapper())
                .build();
    }

    private static void createData(TestDatabase testDb) throws SQLException {
        try (var con = testDb.getConnection(); var stmt = con.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Reminder");
            stmt.execute("CREATE TABLE Reminder (id BIGINT PRIMARY KEY, label VARCHAR(40) NOT NULL, due DATE NULL)");
            stmt.execute("INSERT INTO Reminder VALUES (1, 'with', '2026-10-06')");
            stmt.execute("INSERT INTO Reminder VALUES (2, 'without', NULL)");
            stmt.execute("DROP TABLE IF EXISTS Note");
            stmt.execute("CREATE TABLE Note (id BIGINT PRIMARY KEY, reminderId BIGINT NOT NULL, text VARCHAR(40) NOT NULL)");
            stmt.execute("INSERT INTO Note VALUES (1, 1, 'dated'), (2, 2, 'undated'), (3, 99, 'orphan')");
        }
    }

    /** Loading whole rows is fine: the record is assembled from the plain column. */
    @TestTemplate
    public void wholeRowsKeepWorking(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var rows = database(testDb).from(Reminder.class).orderBy(r -> r.id()).list();

        assertThat(rows.get(0).getDue()).contains(LocalDate.of(2026, 10, 6));
        assertThat(rows.get(1).getDue()).isEmpty();
    }

    /**
     * The bug. The list is typed as {@code List<Optional<LocalDate>>}, so every element must be an Optional.
     * Today the elements are the raw column values, so reading the first one throws a ClassCastException.
     */
    @TestTemplate
    public void projectingTheBareGetterGivesOptionals(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var dues = database(testDb).from(Reminder.class).orderBy(r -> r.id()).list(r -> r.getDue());

        Optional<LocalDate> first = dues.get(0);
        Optional<LocalDate> second = dues.get(1);
        assertThat(first).contains(LocalDate.of(2026, 10, 6));
        assertThat(second).isEmpty();
    }
    
    @TestTemplate
    public void getCanBeProjected(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var dues = database(testDb).from(Reminder.class).where(r -> r.getDue().isPresent())
                .list(r -> r.getDue().get());

        assertThat(dues).containsExactly(LocalDate.of(2026, 10, 6));
    }

    /** The same projection through a method reference, which is decoded differently from a lambda. */
    @TestTemplate
    public void projectingTheBareGetterByMethodReference(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var dues = database(testDb).from(Reminder.class).orderBy(r -> r.id()).list(Reminder::getDue);

        assertThat(dues).containsExactly(Optional.of(LocalDate.of(2026, 10, 6)), Optional.empty());
    }

    @TestTemplate
    public void theGetterNextToAnotherColumn(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var rows = database(testDb).from(Reminder.class).orderBy(r -> r.id()).list(r -> r.label(), r -> r.getDue());

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).v1()).isEqualTo("with");
        assertThat(rows.get(0).v2()).isEqualTo(Optional.of(LocalDate.of(2026, 10, 6)));
        assertThat(rows.get(1).v1()).isEqualTo("without");
        assertThat(rows.get(1).v2()).isEqualTo(Optional.empty());
    }

    /** A left join with no match has no reminder at all, which reads the same as a reminder with no date. */
    @TestTemplate
    public void theGetterAcrossALeftJoin(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var dues = database(testDb).from(Note.class)
                .leftJoin(Reminder.class, (n, r) -> n.reminderId() == r.id())
                .orderBy((n, r) -> n.id())
                .list((n, r) -> r.getDue());

        assertThat(dues).containsExactly(Optional.of(LocalDate.of(2026, 10, 6)), Optional.empty(), Optional.empty());
    }

    /** {@code get()} gives the value itself, not an Optional, even though the accessor returns one. */
    @TestTemplate
    public void getAcrossALeftJoinIsNotWrapped(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var dues = database(testDb).from(Note.class)
                .leftJoin(Reminder.class, (n, r) -> n.reminderId() == r.id())
                .orderBy((n, r) -> n.id())
                .list((n, r) -> r.getDue().get());

        assertThat(dues).containsExactly(LocalDate.of(2026, 10, 6), null, null);
    }

    @Test
    void placeholder() {
        // keeps the class discoverable when no database template runs
        assertEquals(1, 1);
    }
}
