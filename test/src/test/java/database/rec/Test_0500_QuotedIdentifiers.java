package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import me.legrange.typelink.Database;
import me.legrange.typelink.IdentifierMode;
import me.legrange.typelink.TableMapper;
import me.legrange.typelink.builder.DatabaseBuilder;
import org.junit.jupiter.api.TestTemplate;
import rec.Group;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A table called {@code Group} - an SQL reserved word - can only be queried when its name is quoted,
 * and the quote character differs per database (back-tick on MariaDB, double quote on PostgreSQL and
 * H2). The table is created with a quoted, mixed-case name, as a mapper reporting "Group" expects.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0500_QuotedIdentifiers extends DatabaseTest {

    /**
     * @param quoteColumns true for a schema created with quoted exact-case column names (what
     *                     {@link IdentifierMode#ALWAYS} expects); false for the usual layout where only
     *                     the reserved-word table name had to be quoted
     */
    private static void createGroupTable(TestDatabase testDb, boolean quoteColumns) throws SQLException {
        try (var con = testDb.getConnection(); var stmt = con.createStatement()) {
            var q = con.getMetaData().getIdentifierQuoteString();
            var c = quoteColumns ? q : "";
            stmt.execute("DROP TABLE IF EXISTS " + q + "Group" + q);
            stmt.execute("CREATE TABLE " + q + "Group" + q + " (" + c + "id" + c + " BIGINT PRIMARY KEY, "
                    + c + "name" + c + " VARCHAR(50))");
            stmt.execute("INSERT INTO " + q + "Group" + q + " VALUES (1, 'admins'), (2, 'users')");
        }
    }

    private static Database<Record> database(TestDatabase testDb, IdentifierMode mode) {
        var withMapper = DatabaseBuilder.of(Record.class)
                .connection(() -> {
                    try {
                        return testDb.getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .mapper(TableMapper.RECORD_MAPPER);
        return withMapper.identifiers(mode).build();
    }

    @TestTemplate
    public void autoModeQuotesTheReservedWordTable(TestDatabase testDb) throws SQLException {
        createGroupTable(testDb, false);

        var groups = database(testDb, IdentifierMode.AUTO).from(Group.class).where(g -> g.name().equals("users")).list();

        assertEquals(1, groups.size());
        assertEquals(2L, groups.getFirst().id());
    }

    @TestTemplate
    public void alwaysModeQuotesTheReservedWordTable(TestDatabase testDb) throws SQLException {
        createGroupTable(testDb, true);

        var groups = database(testDb, IdentifierMode.ALWAYS).from(Group.class).where(g -> g.name().equals("users")).list();

        assertEquals(1, groups.size());
        assertEquals(2L, groups.getFirst().id());
    }

    @TestTemplate
    public void defaultBuilderModeIsAuto(TestDatabase testDb) throws SQLException {
        createGroupTable(testDb, false);

        var db = DatabaseBuilder.of(Record.class)
                .connection(() -> {
                    try {
                        return testDb.getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .mapper(TableMapper.RECORD_MAPPER)
                .build();

        assertEquals(2, db.from(Group.class).list().size());
    }

    @TestTemplate
    public void reservedWordTableFailsWhenQuotingIsDisabled(TestDatabase testDb) throws SQLException {
        createGroupTable(testDb, false);

        var db = database(testDb, IdentifierMode.NEVER);
        assertThrows(RuntimeException.class, () -> db.from(Group.class).list());
    }
}
