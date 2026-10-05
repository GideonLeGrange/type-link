package database.rec;

import me.legrange.typelink.SqlDatabase;
import me.legrange.typelink.TableMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Results are read by position in the select list, not by matching the table and column names the
 * database reports. These tables are declared with their columns in a different order from the
 * records that map them, and with a column name ({@code id}, {@code name}) shared between them, so
 * a reader that trusted either {@code Table.*} ordering or result-set metadata would hand back the
 * wrong values.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_0900_ColumnOrder {

    private static final String URL = "jdbc:h2:mem:column_order;DB_CLOSE_DELAY=-1";

    public record Maker(Integer id, String name) {
    }

    public record Widget(Integer id, Integer makerId, String name, Double price) {
    }

    public record Pair(Widget widget, Maker maker) {
    }

    private static Connection keepAlive;
    private static SqlDatabase<Record> db;

    @BeforeAll
    static void setUp() throws SQLException {
        keepAlive = DriverManager.getConnection(URL);
        try (var statement = keepAlive.createStatement()) {
            // Every column deliberately out of the order the records declare them in.
            statement.execute("CREATE TABLE Maker (name VARCHAR(50), id INT)");
            statement.execute("CREATE TABLE Widget (price DOUBLE, name VARCHAR(50), makerId INT, id INT)");
            statement.execute("INSERT INTO Maker (name, id) VALUES ('Acme', 1), ('Globex', 2)");
            statement.execute("INSERT INTO Widget (price, name, makerId, id) VALUES (9.5, 'sprocket', 1, 10), (3.25, 'gear', 2, 11)");
        }
        db = new SqlDatabase<>(() -> {
            try {
                return DriverManager.getConnection(URL);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, TableMapper.RECORD_MAPPER);
    }

    @AfterAll
    static void tearDown() throws SQLException {
        keepAlive.close();
    }

    @Test
    public void aWholeTableIsReadByColumnNameNotDatabaseOrder() {
        assertThat(db.from(Widget.class).orderBy(Widget::id).list()).containsExactly(
                new Widget(10, 1, "sprocket", 9.5),
                new Widget(11, 2, "gear", 3.25));
    }

    @Test
    public void tablesSharingColumnNamesAreEachReadFromTheirOwnColumns() {
        var rows = db.from(Widget.class)
                .join(Maker.class, (w, m) -> w.makerId().equals(m.id()))
                .orderBy((w, _) -> w.id())
                .list(Pair::new);
        assertThat(rows).containsExactly(
                new Pair(new Widget(10, 1, "sprocket", 9.5), new Maker(1, "Acme")),
                new Pair(new Widget(11, 2, "gear", 3.25), new Maker(2, "Globex")));
    }

    @Test
    public void aFromOverTwoTablesReadsBoth() {
        var rows = db.from(Widget.class, Maker.class)
                .where((w, m) -> w.makerId().equals(m.id()))
                .orderBy((w, _) -> w.id())
                .list(Pair::new);
        assertThat(rows).containsExactly(
                new Pair(new Widget(10, 1, "sprocket", 9.5), new Maker(1, "Acme")),
                new Pair(new Widget(11, 2, "gear", 3.25), new Maker(2, "Globex")));
    }

    @Test
    public void projectedColumnsKeepTheirPositions() {
        var rows = db.from(Widget.class)
                .orderBy(Widget::id)
                .list(Widget::name, Widget::price);
        assertThat(rows.getFirst().v1()).isEqualTo("sprocket");
        assertThat(rows.getFirst().v2()).isEqualTo(9.5);
    }
}
