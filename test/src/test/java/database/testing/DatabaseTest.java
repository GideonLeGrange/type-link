package database.testing;

import me.legrange.typelink.*;
import me.legrange.typelink.builder.DatabaseBuilder;
import me.legrange.typelink.sql.unpack.ResultSetFunctions;
import me.legrange.typelink.sql.unpack.Rows;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static me.legrange.typelink.sql.unpack.Readers.getReaders;
import static me.legrange.typelink.sql.unpack.Readers.makeObjectReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ExtendWith(DatabaseInvocationContextProvider.class)
public abstract class DatabaseTest {

    @SafeVarargs
    private static <R extends Row, T> List<T> select(TestDatabase db, String query, Function<R, T> function, Class<? extends Record>... types) throws SQLException {
        var rows = (List<R>) selectRows(db, query, types);
        var res = new ArrayList<T>();
        for (var row : rows) {
            res.add(function.apply(row));
        }
        return res;
    }

    protected static <T extends Record> List<T> select(TestDatabase db, String query, Class<T> type) throws SQLException {
        try (var con = db.getConnection(); var stmt = con.createStatement(); var rs = stmt.executeQuery(query)) {
            return ResultSetsX.getObjects(rs, type);
        }
    }

    protected static Database<Record> db(TestDatabase db) {
        return newDatabase(db);
    }

    @SafeVarargs
    private static List<?> selectRows(TestDatabase db, String query, Class<? extends Record>... types) throws SQLException {
        try (var con = db.getConnection(); var stmt = con.createStatement(); var rs = stmt.executeQuery(query)) {
            var res = new ArrayList<>();
            var readers = Arrays.stream(types).map(type -> {
                try {
                    return makeObjectReader(TableMapper.RECORD_MAPPER, rs, type);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
            while (rs.next()) {
                res.add(Rows.makeRow(readers.stream().map(reader -> reader.unpack(rs)).toList()));
            }
            return res;
        }
    }

    protected static List<?> select(TestDatabase db, String query, Class<?>... types) throws SQLException {
        try (var con = db.getConnection(); var stmt = con.createStatement(); var rs = stmt.executeQuery(query)) {
            var res = new ArrayList<>();
            var typesList = Arrays.asList(types);
            var cols = typesList.size();
            while (rs.next()) {
                var row = makeRow(rs, typesList);
                if (cols == 1) {
                    res.add(row.get(0));
                } else {
                    res.add(row);
                }
            }
            return res;
        }
    }

    protected static Row selectRow(TestDatabase db, String query, List<? extends Class<?>> types) throws SQLException {
        try (var con = db.getConnection(); var stmt = con.createStatement(); var rs = stmt.executeQuery(query)) {
            rs.next();
            return makeRow(rs, types);
        }
    }

    private static <T extends Number> T selectNumber(TestDatabase db, String query, Class<T> type) throws SQLException {
        try (var con = db.getConnection(); var stmt = con.createStatement(); var rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return (T) ResultSetFunctions.getColumnReader(type).read(rs, 1);
            }
        }
        return null;
    }

    private static <R extends Row> R makeRow(ResultSet rs, List<? extends Class<?>> types) throws SQLException {
        var readers = getReaders(rs, TableMapper.RECORD_MAPPER, (List<Class<?>>) types);
        var values = new Object[readers.size()];
        for (var i = 0; i < readers.size(); ++i) {
            values[i] = readers.get(i).unpack(rs);
        }
        return Rows.makeRow(values);
    }


    protected static Object[] toArray(Row row) {
        var res = new Object[row.columCount()];
        for (var i = 0; i < row.columCount(); ++i) {
            res[i] = row.get(i);
        }
        return res;
    }

    protected final <T extends Record> From1<T> from(TestDatabase db, Class<T> type) {
        return newDatabase(db).from(type);
    }

    protected final <T1 extends Record, T2 extends Record> From2<T1, T2> from(TestDatabase db, Class<T1> type1, Class<T2> type2) {
        return newDatabase(db).from(type1, type2);
    }

    protected final <T1 extends Record, T2 extends Record, T3 extends Record> From3<T1, T2, T3> from(TestDatabase db, Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        return newDatabase(db).from(type1, type2, type3);
    }

    protected final <R extends Row, T> void testListOfCustom(TestDatabase db, String query, List<T> testData, Function<R, T> function, Class<? extends Record>... types) throws SQLException {
        var controlData = select(db, query, function, types);
        assertThat(testData)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(controlData);
    }

    protected final <T extends Record> void testListOfRecord(TestDatabase db, String query, List<T> testData, Class<T> type) throws SQLException {
        var controlData = select(db, query, type);
        assertThat(testData)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(controlData);
    }

    protected final <T extends Row> void testListOfRow(TestDatabase db, String query, List<T> testData, Class<?>... types) throws SQLException {
        var controlData = select(db, query, types);
        assertThat(testData)
                .as("The SQL result must not be null")
                .isNotNull()
                .as("The SQL result must not be empty")
                .isNotEmpty()
                .as("The SQL result must be %d in length", controlData.size())
                .hasSameSizeAs(controlData)
                .as("The SQL result must be the same as the control data")
                .isEqualTo(controlData);
        for (var row : testData) {
            assertThat(row)
                    .isInstanceOf(Row.class);
            switch (row) {
                case Row1<?> row1 -> {
                }
                case Row2<?,?> row2 -> {
                    row2.v1();
                    row2.v2();
                }
                case Row3 row3 -> {
                    row3.v1();
                    row3.v2();
                    row3.v3();
                }
                case Row4 row4 -> {
                    row4.v1();
                    row4.v2();
                    row4.v3();
                    row4.v4();
                }
            };
            break;
        }
    }

    protected final void assertExpected(List<?> want, List<?> have) {
        assertThat(have)
                .as("The SQL result must not be null")
                .isNotNull()
                .as("The SQL result must not be empty")
                .isNotEmpty()
                .as("The SQL result must be %d in length", want.size())
                .hasSameSizeAs(want)
                .as("The SQL result must be the same as the control data")
                .isEqualTo(want);
    }

    protected final void assertExpected(Row want, Row have) {
        assertThat(have)
                .as("The SQL result must not be null")
                .isNotNull()
                .as("The SQL result must be the same as the control data")
                .isEqualTo(want);
    }

    protected final <T extends Number> void testNumber(TestDatabase db, String query, T value, Class<T> type) throws SQLException {
        var controlData = selectNumber(db, query, type);
        assertThat(value).isNotNull();
        assertThat(controlData).isNotNull();
        if (value instanceof Double || value instanceof Float) {
            // Floating-point aggregates can legitimately differ in their last bit or two between a
            // literal SQL value and the same value bound as a PreparedStatement parameter - the two
            // are evaluated through slightly different paths in the driver/DB. Bit-exact equality
            // isn't a meaningful assertion for an independently-computed double; a tiny tolerance is.
            assertThat(value.doubleValue()).isCloseTo(controlData.doubleValue(), within(1e-9));
        } else {
            assertThat(value).isEqualTo(controlData);
        }
    }

    protected final void testRow(TestDatabase db, String query, Row row) throws SQLException {
        var controlData = selectRow(db, query, Arrays.stream(toArray(row)).map(Object::getClass).toList());
        assertThat(row)
                .isNotNull()
                .isEqualTo(controlData);
    }

    private static Database<Record> newDatabase(TestDatabase db) {
        return DatabaseBuilder.of(Record.class)
                .connection(() -> {
                    try {
                        return db.getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .mapper(TableMapper.RECORD_MAPPER)
                .build();
    }

}
