package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import me.legrange.typelink.QueryPredicate1;
import org.junit.jupiter.api.TestTemplate;
import rec.Nic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Many shapes of AND / OR / NOT over four independent conditions, each checked against evaluating the very same
 * lambda in Java over every combination of the conditions. The database must select exactly the rows the lambda
 * accepts.
 * <p>
 * The conditions: A = address contains "10.0", B = walled, C = hostId == 1, D = id > 8. Row {@code i} (0..15) has
 * A, B, C and D set from the bits of {@code i}, so all sixteen combinations exist exactly once.
 */
@SuppressWarnings({"NewClassNamingConvention", "PointlessBooleanExpression", "Convert2MethodRef"})
public final class Test_0314_BooleanShapes extends DatabaseTest {

    private static List<Nic> createData(TestDatabase testDb) throws SQLException {
        var rows = new ArrayList<Nic>();
        try (var con = testDb.getConnection(); var stmt = con.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Nic");
            stmt.execute("CREATE TABLE Nic (id BIGINT PRIMARY KEY, hostId BIGINT, address VARCHAR(32), walled BOOLEAN)");
            for (int i = 0; i < 16; i++) {
                var a = (i & 1) != 0;
                var b = (i & 2) != 0;
                var c = (i & 4) != 0;
                var row = new Nic(i + 1L, c ? 1L : 2L, (a ? "10.0.0." : "192.168.0.") + i, b);
                rows.add(row);
                stmt.execute("INSERT INTO Nic VALUES (" + row.id() + ", " + row.hostId() + ", '" + row.address() + "', " + row.walled() + ")");
            }
        }
        return rows;
    }

    private void check(TestDatabase testDb, List<Nic> rows, QueryPredicate1<Nic> predicate) throws SQLException {
        var want = rows.stream().filter(predicate::test).map(Nic::id).sorted().toList();
        var have = from(testDb, Nic.class).where(predicate).list().stream().map(Nic::id).sorted().toList();
        assertThat(have).isEqualTo(want);
    }

    @TestTemplate
    public void testShapes(TestDatabase testDb) throws SQLException {
        var rows = createData(testDb);
        var failures = new ArrayList<String>();
        int index = 0;
        for (var shape : shapes()) {
            index++;
            try {
                check(testDb, rows, shape);
            } catch (AssertionError | RuntimeException e) {
                failures.add("shape #" + index + ": " + e.getMessage().replaceAll("\\s+", " "));
            }
        }
        assertThat(failures).as("shapes that did not match their Java evaluation").isEmpty();
    }

    private static List<QueryPredicate1<Nic>> shapes() {
        return List.of(
                n -> n.address().contains("10.0") && n.walled(),
                n -> n.address().contains("10.0") || n.walled(),
                n -> !n.walled(),
                n -> n.address().contains("10.0") && !n.walled(),
                n -> !n.address().contains("10.0") && !n.walled(),
                n -> !n.address().contains("10.0") || n.walled(),
                n -> (n.address().contains("10.0") && !n.walled()) || n.hostId() == 1,
                n -> (n.address().contains("10.0") && n.walled()) || n.hostId() == 1,
                n -> n.hostId() == 1 || (n.address().contains("10.0") && !n.walled()),
                n -> n.address().contains("10.0") && (n.walled() || n.hostId() == 1),
                n -> n.address().contains("10.0") && (!n.walled() || n.hostId() == 1),
                n -> (n.address().contains("10.0") || n.walled()) && n.hostId() == 1,
                n -> (n.address().contains("10.0") || n.walled()) && n.hostId() != 1,
                n -> (n.address().contains("10.0") || !n.walled()) && n.hostId() == 1,
                n -> (n.address().contains("10.0") && n.walled()) || (n.hostId() == 1 && n.id() > 8),
                n -> (n.address().contains("10.0") && !n.walled()) || (n.hostId() == 1 && n.id() <= 8),
                n -> (n.address().contains("10.0") || n.walled()) && (n.hostId() == 1 || n.id() > 8),
                n -> (n.address().contains("10.0") || !n.walled()) && (n.hostId() != 1 || n.id() > 8),
                n -> ((n.address().contains("10.0") && n.walled()) || n.hostId() == 1) && n.id() > 8,
                n -> ((n.address().contains("10.0") && !n.walled()) || n.hostId() == 1) && n.id() <= 8,
                n -> (n.address().contains("10.0") && (n.walled() || n.hostId() == 1)) || n.id() > 8,
                n -> (n.address().contains("10.0") && (!n.walled() || n.hostId() == 1)) || n.id() > 8,
                n -> n.address().contains("10.0") && n.walled() && n.hostId() == 1 || n.id() > 8,
                n -> n.address().contains("10.0") && !n.walled() && n.hostId() == 1 || n.id() > 8,
                n -> n.address().contains("10.0") || n.walled() || n.hostId() == 1 && n.id() > 8,
                n -> n.address().contains("10.0") || !n.walled() || n.hostId() == 1 && n.id() <= 8,
                n -> (n.address().contains("10.0") && !n.walled()) || (n.hostId() == 1 && n.id() <= 8) || n.id() > 12,
                n -> (n.address().contains("10.0") && n.walled()) || (n.hostId() == 1 && n.id() > 8)
                        || (!n.address().contains("10.0") && n.hostId() != 1),
                n -> ((n.address().contains("10.0") || n.walled()) && n.hostId() == 1) || n.id() > 8,
                n -> ((n.address().contains("10.0") || !n.walled()) && n.hostId() == 1) || n.id() > 12,
                n -> n.address().contains("10.0") && (n.walled() || (n.hostId() == 1 && n.id() > 8)),
                n -> n.address().contains("10.0") && (!n.walled() || (n.hostId() == 1 && n.id() <= 8)),
                n -> !n.address().contains("10.0") || (n.walled() && (n.hostId() != 1 || n.id() > 8)),
                n -> !n.address().contains("10.0") || (!n.walled() && (n.hostId() != 1 || n.id() <= 8)),
                n -> (!n.address().contains("10.0") && !n.walled()) || n.hostId() == 1,
                n -> (!n.address().contains("10.0") || n.walled()) && (n.hostId() == 1 || !(n.id() > 8)),
                n -> n.walled() || (n.address().contains("10.0") && !n.walled()),
                n -> (n.walled() && !n.address().contains("10.0")) || n.hostId() == 1
        );
    }
}
