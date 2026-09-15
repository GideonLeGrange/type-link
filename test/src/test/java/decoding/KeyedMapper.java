package decoding;

import me.legrange.typelink.BeanMapper;
import me.legrange.typelink.TableMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * A mapper for which one inherited accessor names a different column in every table.
 *
 * <p>That is the shape an ORM takes when a base class offers a generic way to reach a member each
 * subtype names for itself - ObjDB's {@code Obj.getObjKey()}, which reads whichever field that type
 * declared as its key. The method is declared once, on the base class, and says nothing about which
 * column it means; only the type does.
 *
 * <p>Here {@code Entity.getKey()} stands for that accessor, and each subtype's real column is its
 * own name with {@code Id} appended. Everything else is a {@link BeanMapper}, which is final, so
 * this delegates rather than extends.
 */
final class KeyedMapper<B> implements TableMapper<B> {

    private final BeanMapper<B> delegate;

    KeyedMapper(Class<B> superType) {
        this.delegate = new BeanMapper<>(superType);
    }

    // --- the point of the fixture ---------------------------------------------

    @Override
    public boolean isColumn(Class<?> type, Method method) {
        return isGenericKey(method) || delegate.isColumn(method);
    }

    @Override
    public String columnName(Class<?> type, Method method) {
        return isGenericKey(method) ? keyColumn(type) : delegate.columnName(method);
    }

    @Override
    public boolean isColumn(Class<?> type, Field field) {
        return isGenericKey(field) || delegate.isColumn(field);
    }

    @Override
    public String columnName(Class<?> type, Field field) {
        return isGenericKey(field) ? keyColumn(type) : delegate.columnName(field);
    }

    private static String keyColumn(Class<?> type) {
        return Character.toLowerCase(type.getSimpleName().charAt(0))
                + type.getSimpleName().substring(1) + "Id";
    }

    private static boolean isGenericKey(Method method) {
        return "getKey".equals(method.getName());
    }

    private static boolean isGenericKey(Field field) {
        return "key".equals(field.getName());
    }

    // --- plain delegation ------------------------------------------------------

    @Override
    public boolean isTable(Class<? extends B> type) {
        return delegate.isTable(type);
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
