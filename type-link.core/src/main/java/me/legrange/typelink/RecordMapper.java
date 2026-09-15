package me.legrange.typelink;

import me.legrange.typelink.sql.unpack.UnpackException;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

final class RecordMapper<R extends Record> implements TableMapper<R> {

    private static final Map<Class<? extends Record> , RecordMap> typeIdx = new HashMap<>();

    private final Class<R> superType;

    RecordMapper(Class<R> superType) {
        this.superType = superType;
    }

    @Override
    public boolean isTable(Class<? extends R> type) {
        return superType.isAssignableFrom(type);
    }

    @Override
    public String tableName(Class<? extends R> type) {
        return type.getSimpleName();
    }

    @Override
    public String columnName(Method method) {
        return method.getName();
    }

    @Override
    public String columnName(Field field) {
        return field.getName();
    }

    @Override
    public boolean isColumn(Method method) {
        var type = method.getDeclaringClass();
        return superType.isAssignableFrom(type)
                 && method.getParameterCount() == 0
                 && Arrays.stream(type.getRecordComponents()).map(RecordComponent::getAccessor)
                 .anyMatch(m -> m.equals(method));
    }

    @Override
    public boolean isColumn(Field field) {
        return superType.isAssignableFrom(field.getDeclaringClass())
                && Arrays.stream(field.getDeclaringClass().getRecordComponents())
                .anyMatch(rc -> rc.getName().equals(field.getName()));
    }

    @Override
    public List<String> columnNames(Class<? extends R> type) {
        if (superType.isAssignableFrom(type)) {
            return Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList();
        }
        return List.of();
    }

    @Override
    public Class<?> columnType(Class<? extends R> type, String name) {
        return Arrays.stream(type.getRecordComponents())
                .filter(c -> c.getName().equals(name))
                .map(RecordComponent::getType)
                .findFirst().orElseThrow(() -> new RuntimeException(format("Can't find component '%s' on record %s", name, type.getSimpleName())));
    }


    @Override
    public Object assemble(Class<? extends R> type, Map<String, Object> values) {
        try {
            return makeRecord(getTypeMap(type), values);
        } catch (SQLException e) {
            throw new UnpackException(e.getMessage(), e);
        }
    }

    private Object makeRecord(RecordMap recordMap, Map<String, ?> values) throws UnpackException {
        var comps = new Object[recordMap.fields().size()];
        for (var name : recordMap.fields().keySet()) {
            var field = recordMap.fields().get(name);
            var val =  values.get(name);
            if (isEnum(field)) {
                val = mapEnumValue(field, val);
            }
            comps[field.index()] = val;
        }
        try {
            return recordMap.constructor().newInstance(comps);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new UnpackException(e.getMessage(), e);
        }
    }

    private boolean isEnum(FieldMap field) {
        return Enum.class.isAssignableFrom(field.type());
    }

    private Object mapEnumValue(FieldMap field, Object val) throws UnpackException {
        @SuppressWarnings("unchecked") var eType = (Class<Enum<?>>) field.type();
        var opt = Arrays.stream(eType.getEnumConstants()).filter(e -> e.name().equals(val))
                .findFirst();
        if (opt.isEmpty()) {
            throw new UnpackException(format("Can't find enum value %s", val));
        }
        return opt.get();
    }

    private  RecordMap getTypeMap(Class<? extends R> type) throws SQLException {
        var typeMap = typeIdx.get(type);
        if (typeMap == null) {
            typeMap = makeTypeMap(type);
            typeIdx.put(type, typeMap);
        }
        return typeMap;
    }

    private static RecordMap makeTypeMap(Class<?> type) throws SQLException {
        if (type.isRecord()) {
            return makeRecordMap(type);
        } else {
            throw new IllegalArgumentException(format("Type %s is not a record", type.getSimpleName()));
        }
    }

    private static RecordMap makeRecordMap(Class<?> type) throws SQLException {
        var fields = new HashMap<String, FieldMap>();
        var pos = 0;
        var params = new Class<?>[type.getRecordComponents().length];
        for (var rc : type.getRecordComponents()) {
            fields.put(rc.getName(), new FieldMap(rc.getName(), rc.getType(), pos));
            params[pos] = rc.getType();
            pos++;
        }
        try {
            return new RecordMap(fields, type.getConstructor(params));
        } catch (NoSuchMethodException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    private record FieldMap(String fieldName, Class<?> type, int index) {}

    private record RecordMap(Map<String, FieldMap> fields, Constructor<?> constructor) {
    }

}
