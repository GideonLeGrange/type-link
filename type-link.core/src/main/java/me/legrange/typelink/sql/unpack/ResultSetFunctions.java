package me.legrange.typelink.sql.unpack;

import java.math.BigDecimal;
import java.sql.ResultSet;
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
        functions.put(Double.TYPE, ResultSet::getDouble);
        functions.put(Float.TYPE, ResultSet::getFloat);
        functions.put(Long.TYPE, ResultSet::getLong);
        functions.put(Integer.TYPE, ResultSet::getInt);
        functions.put(Short.TYPE, ResultSet::getShort);
        functions.put(Byte.TYPE, ResultSet::getByte);
        functions.put(Boolean.TYPE, ResultSet::getBoolean);
        functions.put(String.class, ResultSet::getString);
        functions.put(LocalDate.class, (rs, index) -> rs.getObject(index, LocalDate.class));
        functions.put(LocalDateTime.class, (rs, index) -> rs.getObject(index, LocalDateTime.class));
        functions.put(Date.class, ResultSet::getDate);
    }

    public static ColumnReader<?> getColumnReader(Class<?> type) {
       if (functions.containsKey(type)) {
           return functions.get(type);
       }
       return ResultSet::getObject;
    }

}