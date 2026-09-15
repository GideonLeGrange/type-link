package decoding;

import me.legrange.typelink.TableMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * A {@link TableMapper} that maps chosen types to a table of a different name, delegating
 * everything else to the record mapper.
 *
 * <p>Written by delegation rather than by extending {@code RecordMapper}, which is final and
 * package private. Nothing about that needs changing for this: a mapper is an interface, and this
 * is what implementing it from outside the library looks like.
 */
final class RenamingMapper implements TableMapper<Record> {

    private final TableMapper<Record> delegate = TableMapper.RECORD_MAPPER;
    private final Map<Class<?>, String> names;

    RenamingMapper(Map<Class<?>, String> names) {
        this.names = Map.copyOf(names);
    }

    @Override
    public String tableName(Class<? extends Record> type) {
        var renamed = names.get(type);
        return renamed != null ? renamed : delegate.tableName(type);
    }

    // --- everything below is the record mapper's behaviour, unchanged --------

    @Override
    public boolean isTable(Class<? extends Record> type) {
        return delegate.isTable(type);
    }

    @Override
    public boolean isColumn(Method method) {
        return delegate.isColumn(method);
    }

    @Override
    public boolean isColumn(Field field) {
        return delegate.isColumn(field);
    }

    @Override
    public String columnName(Method method) {
        return delegate.columnName(method);
    }

    @Override
    public String columnName(Field field) {
        return delegate.columnName(field);
    }

    @Override
    public List<String> columnNames(Class<? extends Record> type) {
        return delegate.columnNames(type);
    }

    @Override
    public Class<?> columnType(Class<? extends Record> type, String name) {
        return delegate.columnType(type, name);
    }

    @Override
    public Object assemble(Class<? extends Record> type, Map<String, Object> values) {
        return delegate.assemble(type, values);
    }
}
