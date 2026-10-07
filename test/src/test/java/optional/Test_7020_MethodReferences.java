package optional;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import me.legrange.typelink.Database;
import me.legrange.typelink.builder.DatabaseBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Experimental. A method reference names a method instead of containing code. If the mapper says the method is a
 * column it means that column; otherwise it means the method's body.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_7020_MethodReferences extends DatabaseTest {

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
            stmt.execute("INSERT INTO Reminder VALUES (1, 'b', '2026-10-06'), (2, 'a', NULL)");
        }
    }

    @TestTemplate
    public void aColumnReferenceCanBeProjected(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var labels = database(testDb).from(Reminder.class).orderBy(Reminder::label).list(Reminder::label);

        assertThat(labels).containsExactly("a", "b");
    }

    /** {@code isDated} is not a column, so the reference expands to what its body does. */
    @TestTemplate
    public void aReferenceToAMethodWithABodyIsExpanded(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var labels = database(testDb).from(Reminder.class).where(Reminder::isDated).list(Reminder::label);

        assertThat(labels).containsExactly("b");
    }

    @Test
    void theExpandedBodyIsTheSqlOfTheBody() {
        var db = new CapturingSql();
        db.from().where(Reminder::isDated).list();

        assertThat(db.sql()).endsWith("WHERE Reminder.due IS NOT NULL");
    }

    @Test
    void aColumnReferenceInOrderByNamesTheColumn() {
        var db = new CapturingSql();
        db.from().orderBy(Reminder::label).list();

        assertEquals("ORDER BY Reminder.label", db.sql().substring(db.sql().indexOf("ORDER BY")));
    }

    @TestTemplate
    public void aJoinConditionCanBeAMethodReference(TestDatabase testDb) throws SQLException {
        createJoinData(testDb);

        var texts = database(testDb).from(Note.class)
                .join(Reminder.class, Note::pointsAt)
                .orderBy((n, r) -> n.id())
                .list((n, r) -> n.text());

        assertThat(texts).containsExactly("dated", "undated");
    }

    @TestTemplate
    public void aWhereOverAJoinCanBeAMethodReference(TestDatabase testDb) throws SQLException {
        createJoinData(testDb);

        var texts = database(testDb).from(Note.class)
                .leftJoin(Reminder.class, (n, r) -> n.reminderId() == r.id())
                .where(Note::pointsAt)
                .orderBy((n, r) -> n.id())
                .list((n, r) -> n.text());

        assertThat(texts).containsExactly("dated", "undated");
    }

    @Test
    void aJoinConditionReferenceExpandsToTheBody() {
        var db = new CapturingSql();
        db.database().from(Note.class).join(Reminder.class, Note::pointsAt).list((n, r) -> n);

        assertThat(db.sql()).contains("INNER JOIN Reminder ON Note.reminderId = Reminder.id");
    }

    /** The accessor is declared on an interface; the mapper is asked about it for the table being read. */
    @TestTemplate
    public void anInterfaceDeclaredAccessorCanBeProjected(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var labels = database(testDb).from(Reminder.class).orderBy(Labelled::label).list(Labelled::label);

        assertThat(labels).containsExactly("a", "b");
    }

    /** The same reference through the library's own record mapper, with nothing custom in between. */
    @TestTemplate
    public void anInterfaceDeclaredAccessorNeedsNoCustomMapper(TestDatabase testDb) throws SQLException {
        createData(testDb);

        var labels = from(testDb, Reminder.class).orderBy(Labelled::label).list(Labelled::label);

        assertThat(labels).containsExactly("a", "b");
    }

    private static void createJoinData(TestDatabase testDb) throws SQLException {
        createData(testDb);
        try (var con = testDb.getConnection(); var stmt = con.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Note");
            stmt.execute("CREATE TABLE Note (id BIGINT PRIMARY KEY, reminderId BIGINT NOT NULL, text VARCHAR(40) NOT NULL)");
            stmt.execute("INSERT INTO Note VALUES (1, 1, 'dated'), (2, 2, 'undated'), (3, 99, 'orphan')");
        }
    }
}
