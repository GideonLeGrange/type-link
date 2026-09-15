package me.legrange.typelink.sql.unpack;

import me.legrange.typelink.*;
import me.legrange.typelink.sql.structure.SqlAll;
import me.legrange.typelink.sql.structure.SqlColumn;
import me.legrange.typelink.sql.structure.SqlFunction;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Rows {
    @SuppressWarnings("unchecked")
    public static <R extends Row> R makeRow(List<?> values) {
           return (R) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{rowType(values.size())},
                new RowProxy(values.toArray())::invoke);
    }
    @SuppressWarnings("unchecked")
    public static <R extends Row> R makeRow(Object[] values) {
        return (R) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[]{rowType(values.length)},
                new RowProxy(values)::invoke);
    }

    private static class RowProxy {

        private final Map<SqlColumn, Integer> index;
        private final Function<SelectFunction, SqlColumn> lookup;
        private final Object[] values;

        private RowProxy(Function<SelectFunction, SqlColumn> lookup, Map<SqlColumn, Integer> index, Object[] values) {
            this.lookup = lookup;
            this.index = index;
            this.values = values;
        }

        private RowProxy(Object[] values) {
            this(_ -> null, Map.of(), values);
        }

        private Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "v1" -> values[0];
                case "v2" -> values[1];
                case "v3" -> values[2];
                case "v4" -> values[3];
                case "columCount" -> values.length;
                case "get" -> {
                    if (args[0] instanceof SelectFunction function) {
                        yield get(index, values, lookup.apply(function));
                    } else {
                        yield values[(int) args[0]];
                    }
                }
                case "equals" -> isEquals(proxy, values);
                case "toString" -> Arrays.stream(values)
                        .filter(Objects::nonNull)
                        .map(Object::toString).collect(Collectors.joining(", "));
                default -> throw new SqlRowException("Unknown method " + method.getName());
            };
        }
    }

    private static boolean isEquals(Object proxy, Object[] values) {
        var row = (Row) proxy;
        if (row.columCount() != values.length) {
            return false;
        }
        for (int i = 0; i < row.columCount(); ++i) {
            if (!Objects.equals(row.get(i), values[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<? extends Row> rowType(int size) {
        return switch (size) {
            case 1 -> Row1.class;
            case 2 -> Row2.class;
            case 3 -> Row3.class;
            case 4 -> Row4.class;
            default -> throw new SqlRowException("Can't make row of size " + size + ". BUG!");
        };
    }

    private static Object get(Map<SqlColumn, Integer> index, Object[] data, SqlColumn col) {
        if (index.containsKey(col)) {
            return data[index.get(col)];
        }
        if (col instanceof SqlAll(List<SqlColumn> cols)) {
            return cols.stream()
                    .map(table -> get(index, data, table)).toArray();
        }
        var newIndex = index.keySet().stream()
                .filter(sc -> sc instanceof SqlFunction)
                .map(sc -> (SqlFunction) sc)
                .collect(Collectors.toMap(SqlFunction::parameter, index::get));
        if (newIndex.isEmpty()) {
            throw new RuntimeException("Cannot find data for column " + col);
        }
        return get(newIndex, data, col);
    }

    private Rows() {
    }

}