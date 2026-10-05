package me.legrange.typelink.sql.unpack;

import me.legrange.typelink.TableMapper;
import me.legrange.typelink.sql.structure.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static me.legrange.typelink.sql.unpack.ResultSetFunctions.getColumnReader;
import static me.legrange.typelink.sql.unpack.Types.typeFor;

public final class Readers {

    private static final Map<Class<?>, Class<?>> primitiveTypes = Map.of(Integer.TYPE, Integer.class,
            Long.TYPE, Long.class,
            Short.TYPE, Short.class,
            Byte.TYPE, Byte.class,
            Double.TYPE, Double.class,
            Float.TYPE, Float.class, Boolean.TYPE, Boolean.class,
            Character.TYPE, Character.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<ResultSetReader> getReaders(ResultSet rs, TableMapper<?> mapper, List<Class<?>> types) throws SQLException {
        var res = new ArrayList<ResultSetReader>();
        var offset = 0;
        var columns = getColumns(rs);
        for (Class type : types) {
            if (mapper.isTable(type)) {
                var tableName = mapper.tableName(type);
                List<String> columnNames = mapper.columnNames(type);
                var columnReaders = new HashMap<String, ResultSetReader>();
                for (var colunmName : columnNames) {
                    columnReaders.put(colunmName, new IndexedColumnReader(indexOf(columns, tableName, colunmName), getColumnReader(resolveType(mapper.columnType(type, colunmName)))));
                }
                var reader = new ObjectReader(mapper, type, columnReaders);
                res.add(reader);
                offset += reader.columnCount();
            } else {
                res.add(new IndexedColumnReader(offset + 1, getColumnReader(resolveType(type))));
                offset++;
            }
        }
        return res;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ObjectReader makeObjectReader(TableMapper mapper, ResultSet rs, Class<?> objectType) throws SQLException {
        var tableName = mapper.tableName(objectType);
        var readers = new HashMap<String, ResultSetReader>();
        List<String> columnNames = mapper.columnNames(objectType);
        var columns = getColumns(rs);
        for (var colunmName : columnNames) {
            readers.put(colunmName, new IndexedColumnReader(indexOf(columns, tableName, colunmName), getColumnReader(resolveType(mapper.columnType(objectType, colunmName)))));
        }
        return new ObjectReader(mapper, objectType, readers);
    }

    /**
     * Readers for the columns a query selects, found by where each one sits in the result set.
     *
     * <p>Positions come from the select list itself, which {@code SqlGenerator} writes out column by
     * column, in order. The alternative - matching {@code ResultSetMetaData} table and column names
     * - only works while the database reports which table a result column came from, and not every
     * result set does: the columns of a {@code UNION}, of a derived table or of an aliased table
     * come back without the table they were read from, or with a different one.
     *
     * @param mapper the table mapper
     * @param column the columns the query selects
     */
    public static List<ResultSetReader> getReaders(TableMapper<?> mapper, SqlColumn column) {
        return getReaders(mapper, column, 0);
    }

    /** The readers for {@code sqlColumn}, which starts {@code offset} columns into the result. */
    private static List<ResultSetReader> getReaders(TableMapper<?> mapper, SqlColumn sqlColumn, int offset) {
        return switch (sqlColumn) {
            case SqlAll sqlAll -> sqlAll(mapper, sqlAll, offset);
            case SqlConstant sqlConstant -> List.of(positional(sqlConstant, offset));
            case SqlFunction sqlFunction -> List.of(positional(sqlFunction, offset));
            case SqlOperation sqlOperation -> List.of(positional(sqlOperation, offset));
            case SqlSubSelect sqlSubSelect -> List.of(positional(sqlSubSelect, offset));
            case SqlTable sqlTable -> List.of(sqlTable(mapper, sqlTable, offset));
            case SqlTableColumn sqlTableColumn -> List.of(positional(sqlTableColumn, offset));
            case SqlConcat sqlConcat -> List.of(positional(sqlConcat, offset));
        };
    }

    private static List<ResultSetReader> sqlAll(TableMapper<?> mapper, SqlAll all, int offset) {
        var res = new ArrayList<ResultSetReader>();
        var next = offset;
        for (var column : all.columns()) {
            var readers = getReaders(mapper, column, next);
            res.addAll(readers);
            next += readers.stream().mapToInt(ResultSetReader::columnCount).sum();
        }
        return res;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ResultSetReader sqlTable(TableMapper mapper, SqlTable table, int offset) {
        List<String> columnNames = table.columns().isEmpty()
                ? mapper.columnNames(table.type())
                : table.columns().stream().map(SqlTableColumn::name).toList();
        var readers = new HashMap<String, ResultSetReader>();
        for (var i = 0; i < columnNames.size(); ++i) {
            var name = columnNames.get(i);
            readers.put(name, new IndexedColumnReader(offset + i + 1, getColumnReader(resolveType(mapper.columnType(table.type(), name)))));
        }
        return new ObjectReader(mapper, table.type(), readers);
    }

    /** A single value in the result, {@code offset} columns in. */
    private static ResultSetReader positional(SqlColumn column, int offset) {
        return new IndexedColumnReader(offset + 1, getColumnReader(resolveType(typeFor(column))));
    }

    private static int indexOf(List<ColumDetail> columns, String tableName, String columnName) {
        var opt = columns.stream()
                .filter(columDetail -> columDetail.tableName().equalsIgnoreCase(tableName))
                .filter(columDetail -> columDetail.columName().equalsIgnoreCase(columnName))
                .map(ColumDetail::index)
                .findAny();
        if (opt.isEmpty()) {
            throw new UnpackException(format("Cannot find %s.%s in the SQL result set. BUG!", tableName, columnName));
        }
        return opt.get();
    }


    private static Class<?>resolveType(Class<?> type) {
        if (type.isPrimitive()) {
            return primitiveTypes.get(type);
        }
        if (Enum.class.isAssignableFrom(type)) {
            return String.class;
        }
        return type;
    }


    private static List<ColumDetail> getColumns(ResultSet rs) throws SQLException {
        var meta = rs.getMetaData();
        var res = new ArrayList<ColumDetail>();
        for (var i = 1; i <= meta.getColumnCount(); ++i) {
            res.add(new ColumDetail(meta.getTableName(i), meta.getColumnName(i), meta.getColumnLabel(i), i));
        }
        return res;
    }

    private Readers() {
    }
}
