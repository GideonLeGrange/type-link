package me.legrange.typelink.sql.unpack;

import java.sql.ResultSet;
import java.sql.SQLException;

record IndexedColumnReader(int index, ColumnReader<?> reader ) implements ResultSetReader {
    @Override
    public Object unpack(ResultSet rs) throws UnpackException {
        try {
            return reader.read(rs, index);
        } catch (SQLException e) {
            throw new UnpackException(e.getMessage(), e);
        }
    }

    @Override
    public int columnCount() {
        return 1;
    }

}
