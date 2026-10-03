package me.legrange.typelink.sql.unpack;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        return value == null || value.isEmpty() ? ifNull : value.charAt(0);
    }

    public static ColumnReader<?> getColumnReader(Class<?> type) {
       if (functions.containsKey(type)) {
           return functions.get(type);
       }
       return ResultSet::getObject;
    }

}