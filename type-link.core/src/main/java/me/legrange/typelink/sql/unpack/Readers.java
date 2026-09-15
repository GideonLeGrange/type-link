package me.legrange.typelink.sql.unpack;

import me.legrange.typelink.TableMapper;
import me.legrange.typelink.sql.structure.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;
import static me.legrange.typelink.sql.unpack.ResultSetFunctions.getColumnReader;
import static me.legrange.typelink.sql.unpack.Types.typeFor;

public final class Readers {

    private static final Map<Class<?>, Class<?>> primitiveTypes = Map.of(Integer.TYPE, Integer.class,
            Long.TYPE, Long.class,
            Short.TYPE, Short.class,
            Byte.TYPE, Byte.class,
            Double.TYPE, Double.class,
            Float.TYPE, Float.class, Boolean.TYPE, Boolean.class);

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

    public static List<ResultSetReader> getReaders(ResultSet rs, TableMapper<?> mapper, SqlColumn column) throws SQLException {
        var columns = getColumns(rs);
        return getReaders(mapper, columns, column, 0);
    }

    private static List<ResultSetReader> getReaders(TableMapper<?> mapper, List<ColumDetail> columns, SqlColumn sqlColumn, int offset) {
        return switch (sqlColumn) {
            case SqlAll sqlAll -> sqlAll(mapper, columns, sqlAll);
            case SqlConstant sqlConstant -> List.of(sqlBasicColumn(sqlConstant));
            case SqlFunction sqlFunction -> List.of(sqlFunction(sqlFunction, offset));
            case SqlOperation sqlOperation -> sqlOperation(mapper, columns, sqlOperation, offset);
            case SqlSubSelect sqlSubSelect -> sqlSubSelect(sqlSubSelect, offset);
            case SqlTable sqlTable -> List.of(sqlTable(mapper, columns, sqlTable));
            case SqlTableColumn sqlTableColumn -> List.of(sqlTableColumn(columns, sqlTableColumn));
            case SqlConcat  sqlConcat -> List.of(sqlConcat(sqlConcat, offset));
        };
    }

    private static List<ResultSetReader> sqlAll(TableMapper<?> mapper, List<ColumDetail> columns, SqlAll all) {
        var sqlColumns = all.columns();
        var res = new ArrayList<ResultSetReader>();
        var offset = 0;
        for (var column : sqlColumns) {
            var reader = getReaders(mapper, columns, column, offset);
            res.addAll(reader);
            offset = offset + reader.stream().mapToInt(ResultSetReader::columnCount).sum();
        }
        return res;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ResultSetReader sqlTable(TableMapper mapper, List<ColumDetail> columns, SqlTable table) {
        var tableName = mapper.tableName(table.type());
        var readers = new HashMap<String, ResultSetReader>();
        List<String> columnNames = mapper.columnNames(table.type());
        for (var colunmName : columnNames) {
            readers.put(colunmName, new IndexedColumnReader(indexOf(columns, tableName, colunmName), getColumnReader(resolveType(mapper.columnType(table.type(), colunmName)))));
        }
        return new ObjectReader(mapper, table.type(), readers);
    }


    private static List<ResultSetReader> sqlSubSelect(SqlSubSelect subSelect, int offset) {
        return List.of(new IndexedColumnReader(offset + 1, getColumnReader(resolveType(typeFor(subSelect)))));
    }

    @SuppressWarnings("rawtypes")
    private static List<ResultSetReader> sqlOperation(TableMapper mapper, List<ColumDetail> columns, SqlOperation operation, int offset) {
        return Stream.concat(getReaders(mapper, columns, operation.left(), offset).stream(), getReaders(mapper, columns, operation.right(), offset).stream()).toList();
    }

    private static ResultSetReader sqlFunction(SqlFunction function, int offset) {
        return new IndexedColumnReader(offset + 1, getColumnReader(resolveType(typeFor(function))));
    }

    private static ResultSetReader sqlConcat(SqlConcat function, int offset) {
        return new IndexedColumnReader(offset + 1, getColumnReader(resolveType(typeFor(function))));
    }

    private static ResultSetReader sqlTableColumn(List<ColumDetail> columns, SqlTableColumn sqlColumn) {
        return new IndexedColumnReader(
                indexOf(columns, sqlColumn.tableName(), sqlColumn.name()),
                getColumnReader(resolveType(sqlColumn.type())));
    }

    private static ResultSetReader sqlBasicColumn(SqlColumn sqlColumn) {
        return new IndexedColumnReader(1, getColumnReader(resolveType(typeFor(sqlColumn))));
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
