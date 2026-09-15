package me.legrange.typelink.sql.unpack;

import java.sql.ResultSet;

public sealed interface ResultSetReader permits IndexedColumnReader, ObjectReader {

    Object unpack(ResultSet rs) throws UnpackException;

    int columnCount();

}
