package me.legrange.typelink;

import me.legrange.typelink.sql.unpack.UnpackException;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * TableMapper implementation for Java Bean-style POJOs with getters and setters.
 *
 * <p>This mapper expects classes to follow JavaBean conventions:
 * <ul>
 *   <li>No-argument constructor</li>
 *   <li>Getter methods named as getXxx() or isXxx() for booleans</li>
 *   <li>Setter methods named as setXxx()</li>
 * </ul>
 *
 * @param <B> the upper bound type for beans to be mapped
 */
public final class BeanMapper<B> implements TableMapper<B> {

    private static final Map<Class<?>, BeanMap> typeIdx = new HashMap<>();

    private final Class<B> superType;

    public BeanMapper(Class<B> superType) {
        if ((superType == null) || Object.class.equals(superType)) {
            throw new IllegalArgumentException("superType must be a non-null class other than Object");
        }
        this.superType = superType;
    }

    @Override
    public boolean isTable(Class<? extends B> type) {
        return superType.isAssignableFrom(type);
    }

    @Override
    public String tableName(Class<? extends B> type) {
        return type.getSimpleName();
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
                && !method.getReturnType().equals(Void.TYPE)
                && isGetter(method);
    }

    @Override
    public boolean isColumn(Field field) {
        if (!superType.isAssignableFrom(field.getDeclaringClass())) {
            return false;
        }
        // Check if there's a corresponding getter method
        var type = field.getDeclaringClass();
        var fieldName = field.getName();
        return hasGetter(type, fieldName);
    }

    @Override
    public String columnName(Method method) {
        if (isGetter(method)) {
            return getPropertyName(method);
        }
        return method.getName();
    }

    @Override
    public List<String> columnNames(Class<? extends B> type) {
        if (superType.isAssignableFrom(type)) {
            try {
                return getTypeMap(type).properties().keySet().stream()
                        .sorted()
                        .collect(Collectors.toList());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return List.of();
    }

    @Override
    public Class<?> columnType(Class<? extends B> type, String name) {
        try {
            var beanMap = getTypeMap(type);
            var property = beanMap.properties().get(name);
            if (property == null) {
                throw new RuntimeException(format("Can't find property '%s' on bean %s", name, type.getSimpleName()));
            }
            return property.type();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object assemble(Class<? extends B> type, Map<String, Object> values) {
        try {
            return makeBean(getTypeMap(type), values);
        } catch (SQLException e) {
            throw new UnpackException(e.getMessage(), e);
        }
    }

    private Object makeBean(BeanMap beanMap, Map<String, ?> values) throws UnpackException {
        try {
            // Create instance using no-arg constructor
            var instance = beanMap.constructor().newInstance();

            // Set values using setters
            for (var entry : values.entrySet()) {
                var propertyName = entry.getKey();
                var property = beanMap.properties().get(propertyName);
                if (property != null && property.setter() != null) {
                    var value = entry.getValue();
                    // Handle enum conversion
                    if (isEnum(property)) {
                        value = mapEnumValue(property, value);
                    }
                    property.setter().invoke(instance, value);
                }
            }

            return instance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new UnpackException(e.getMessage(), e);
        }
    }

    private boolean isEnum(PropertyMap property) {
        return Enum.class.isAssignableFrom(property.type());
    }

    private Object mapEnumValue(PropertyMap property, Object val) throws UnpackException {
        @SuppressWarnings("unchecked") var eType = (Class<Enum<?>>) property.type();
        var opt = Arrays.stream(eType.getEnumConstants())
                .filter(e -> e.name().equals(val))
                .findFirst();
        if (opt.isEmpty()) {
            throw new UnpackException(format("Can't find enum value %s", val));
        }
        return opt.get();
    }

    private BeanMap getTypeMap(Class<? extends B> type) throws SQLException {
        var typeMap = typeIdx.get(type);
        if (typeMap == null) {
            typeMap = makeBeanMap(type);
            typeIdx.put(type, typeMap);
        }
        return typeMap;
    }

    private static BeanMap makeBeanMap(Class<?> type) throws SQLException {
        var properties = new HashMap<String, PropertyMap>();

        // Find all getter methods
        for (var method : type.getMethods()) {
            if (isGetter(method) && !isDeclaredInObject(method)) {
                var propertyName = getPropertyName(method);
                var propertyType = method.getReturnType();

                // Find corresponding setter
                Method setter = findSetter(type, propertyName, propertyType);

                properties.put(propertyName, new PropertyMap(propertyName, propertyType, method, setter));
            }
        }

        try {
            var constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return new BeanMap(properties, constructor);
        } catch (NoSuchMethodException e) {
            throw new SQLException(format("Type %s must have a no-argument constructor", type.getSimpleName()), e);
        }
    }

    private static boolean isGetter(Method method) {
        if (method.getParameterCount() != 0 || method.getReturnType().equals(Void.TYPE)) {
            return false;
        }
        var name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return Character.isUpperCase(name.charAt(3));
        }
        if (name.startsWith("is") && name.length() > 2) {
            return Character.isUpperCase(name.charAt(2)) 
                    && (method.getReturnType().equals(Boolean.TYPE) || method.getReturnType().equals(Boolean.class));
        }
        return false;
    }

    private static boolean isDeclaredInObject(Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    private static String getPropertyName(Method method) {
        var name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return decapitalize(name.substring(3));
        }
        if (name.startsWith("is") && name.length() > 2) {
            return decapitalize(name.substring(2));
        }
        return name;
    }

    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
            // Handle cases like "URL" -> "URL" (not "uRL")
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static Method findSetter(Class<?> type, String propertyName, Class<?> propertyType) {
        var setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            return type.getMethod(setterName, propertyType);
        } catch (NoSuchMethodException e) {
            // Setter is optional - may be read-only property
            return null;
        }
    }

    private static boolean hasGetter(Class<?> type, String fieldName) {
        var getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        var isGetterName = "is" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        for (var method : type.getMethods()) {
            if ((method.getName().equals(getterName) || method.getName().equals(isGetterName))
                    && method.getParameterCount() == 0
                    && !method.getReturnType().equals(Void.TYPE)) {
                return true;
            }
        }
        return false;
    }

    private record PropertyMap(String name, Class<?> type, Method getter, Method setter) {
    }

    private record BeanMap(Map<String, PropertyMap> properties, Constructor<?> constructor) {
    }
}

