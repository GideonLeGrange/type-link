package optional;

import me.legrange.typelink.TableMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Maps {@link Reminder#getDue()} onto the {@code due} column, as ObjDB's mapper does for a {@code @nullable}
 * getter that returns {@code Optional<T>}. Everything else is the record mapper, unchanged.
 */
final class OptionalMapper implements TableMapper<Record> {

    private final TableMapper<Record> delegate = TableMapper.RECORD_MAPPER;

    private static boolean isOptionalGetter(Method method) {
        return method.getName().equals("getDue") && method.getDeclaringClass() == Reminder.class;
    }

    @Override
    public boolean isTable(Class<? extends Record> type) {
        return delegate.isTable(type);
    }

    @Override
    public String tableName(Class<? extends Record> type) {
        return delegate.tableName(type);
    }

    @Override
    public boolean isColumn(Method method) {
        return isOptionalGetter(method) || delegate.isColumn(method);
    }

    @Override
    public boolean isColumn(Class<?> type, Method method) {
        return isOptionalGetter(method) || delegate.isColumn(type, method);
    }

    @Override
    public String columnName(Class<?> type, Method method) {
        return isOptionalGetter(method) ? "due" : delegate.columnName(type, method);
    }

    @Override
    public boolean isColumn(Field field) {
        return delegate.isColumn(field);
    }

    @Override
    public String columnName(Method method) {
        return isOptionalGetter(method) ? "due" : delegate.columnName(method);
    }

    @Override
    public String columnName(Field field) {
        return delegate.columnName(field);
    }

    @Override
    public List<String> columnNames(Class<? extends Record> type) {
        return delegate.columnNames(type);
    }

    /** The column holds a date; the {@code Optional} exists only on the getter. */
    @Override
    public Class<?> columnType(Class<? extends Record> type, String name) {
        return delegate.columnType(type, name);
    }

    @Override
    public Object assemble(Class<? extends Record> type, Map<String, Object> values) {
        return delegate.assemble(type, values);
    }
}
