package me.legrange.typelink.sql.unpack;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ColumnReader<T> {

    T read(ResultSet rs, int index) throws SQLException;

}
