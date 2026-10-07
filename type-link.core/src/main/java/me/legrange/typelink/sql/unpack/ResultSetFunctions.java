package me.legrange.typelink.sql.unpack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class ResultSetFunctions {

    private static final Map<Class<?>, ColumnReader<?>> functions = new HashMap<>();

    static {
        functions.put(BigDecimal.class, ResultSet::getBigDecimal);
        functions.put(Double.class, ResultSet::getDouble);
        functions.put(Float.class, ResultSet::getFloat);
        functions.put(Integer.class, ResultSet::getInt);
        functions.put(Long.class, ResultSet::getLong);
        functions.put(Short.class, ResultSet::getShort);
        functions.put(Byte.class, ResultSet::getByte);
        functions.put(Boolean.class, ResultSet::getBoolean);
        functions.put(Character.class, (rs, index) -> readChar(rs, index, null));
        functions.put(Double.TYPE, ResultSet::getDouble);
        functions.put(Float.TYPE, ResultSet::getFloat);
        functions.put(Long.TYPE, ResultSet::getLong);
        functions.put(Integer.TYPE, ResultSet::getInt);
        functions.put(Short.TYPE, ResultSet::getShort);
        functions.put(Byte.TYPE, ResultSet::getByte);
        functions.put(Boolean.TYPE, ResultSet::getBoolean);
        // Like getInt() and friends, a NULL becomes the type's zero value for a primitive.
        functions.put(Character.TYPE, (rs, index) -> readChar(rs, index, '\0'));
        functions.put(String.class, ResultSet::getString);
        functions.put(LocalTime.class, (rs, index) -> rs.getObject(index, LocalTime.class));
        functions.put(BigInteger.class, (rs, index) -> {
            var value = rs.getBigDecimal(index);
            return value == null ? null : value.toBigInteger();
        });
        functions.put(LocalDate.class, (rs, index) -> rs.getObject(index, LocalDate.class));
        functions.put(LocalDateTime.class, (rs, index) -> rs.getObject(index, LocalDateTime.class));
        // getDate() drops the time of day; getTimestamp() keeps it (and also reads DATE columns).
        functions.put(Date.class, (rs, index) -> {
            var timestamp = rs.getTimestamp(index);
            return timestamp == null ? null : new Date(timestamp.getTime());
        });
    }

    private static Character readChar(ResultSet rs, int index, Character ifNull) throws SQLException {
        var value = rs.getString(index);
        if (value == null || value.isEmpty()) {
            // not a conditional expression: its other branch is a char, which would unbox a null ifNull
            return ifNull;
        }
        return value.charAt(0);
    }

    /**
     * Readers for a column that may hold NULL, keyed by the boxed type that says so.
     *
     * <p>{@code ResultSet.getInt} and its relatives answer 0 (or false) for a NULL, so a boxed
     * {@code Integer} read through them can never be null. These ask {@code wasNull()} afterwards, which every
     * driver supports. {@link #functions} keeps the plain readers for the primitive types, where there is no
     * null to return, and for computed values.
     */
    private static final Map<Class<?>, ColumnReader<?>> nullable = new HashMap<>();

    static {
        nullable.put(Double.class, nullable(ResultSet::getDouble));
        nullable.put(Float.class, nullable(ResultSet::getFloat));
        nullable.put(Integer.class, nullable(ResultSet::getInt));
        nullable.put(Long.class, nullable(ResultSet::getLong));
        nullable.put(Short.class, nullable(ResultSet::getShort));
        nullable.put(Byte.class, nullable(ResultSet::getByte));
        nullable.put(Boolean.class, nullable(ResultSet::getBoolean));
    }

    private static <T> ColumnReader<T> nullable(ColumnReader<T> reader) {
        return (rs, index) -> {
            var value = reader.read(rs, index);
            return rs.wasNull() ? null : value;
        };
    }

    /**
     * The reader for a value the query computes (a constant, an aggregate, arithmetic). A number is always read
     * as a number: a primitive and its boxed type read alike, and a NULL comes back as zero.
     */
    public static ColumnReader<?> getColumnReader(Class<?> type) {
       if (functions.containsKey(type)) {
           return functions.get(type);
       }
       return ResultSet::getObject;
    }

    /**
     * The reader for a table column of the declared type. A primitive type has no null, so it reads as a plain
     * number; a boxed type means the column may be NULL, and keeps it as null.
     */
    public static ColumnReader<?> getDeclaredColumnReader(Class<?> declared) {
        if (nullable.containsKey(declared)) {
            return nullable.get(declared);
        }
        return getColumnReader(declared);
    }

}