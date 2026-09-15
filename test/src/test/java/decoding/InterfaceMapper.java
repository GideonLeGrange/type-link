package decoding;

import me.legrange.typelink.TableMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A mapper for which a column is whatever the <em>table</em> has, however it was reached.
 *
 * <p>The default record mapper asks whether the method was declared on a record, which answers no
 * for an accessor declared on an interface the record implements. An ORM generally cannot work that way: a
 * rule that governs many types reaches their shared members through the interface they share, and
 * those are columns of every one of them.
 */
final class InterfaceMapper<B> implements TableMapper<B> {

    @SuppressWarnings("unchecked")
    private final TableMapper<B> delegate = (TableMapper<B>) TableMapper.RECORD_MAPPER;

    InterfaceMapper(Class<B> superType) {
    }

    @Override
    public boolean isColumn(Class<?> type, Method method) {
        return hasComponent(type, method.getName()) || delegate.isColumn(method);
    }

    @Override
    public boolean isColumn(Class<?> type, Field field) {
        return hasComponent(type, field.getName()) || delegate.isColumn(field);
    }

    private static boolean hasComponent(Class<?> type, String name) {
        return type.isRecord() && Arrays.stream(type.getRecordComponents())
                .anyMatch(component -> component.getName().equals(name));
    }

    /**
     * An ORM's base type is generally a table itself, or near enough that the mapper says yes. That
     * matters because a value of the base type is exactly what a predicate written over a type
     * variable captures.
     */
    @Override
    public boolean isTable(Class<? extends B> type) {
        return Test_6000_EnclosingScopeRows.Entity.class.isAssignableFrom(type) || delegate.isTable(type);
    }

    @Override
    public String tableName(Class<? extends B> type) {
        return delegate.tableName(type);
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
    public List<String> columnNames(Class<? extends B> type) {
        return delegate.columnNames(type);
    }

    @Override
    public Class<?> columnType(Class<? extends B> type, String name) {
        return delegate.columnType(type, name);
    }

    @Override
    public Object assemble(Class<? extends B> type, Map<String, Object> values) {
        return delegate.assemble(type, values);
    }
}
