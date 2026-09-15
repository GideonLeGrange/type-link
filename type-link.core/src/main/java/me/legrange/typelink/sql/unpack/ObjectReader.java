package me.legrange.typelink.sql.unpack;

import me.legrange.typelink.TableMapper;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public record ObjectReader(TableMapper mapper, Class<?> type,
                           Map<String, ResultSetReader> readers) implements ResultSetReader {

    @Override
    public Object unpack(ResultSet rs) throws UnpackException {
        var values = new HashMap<String, Object>(readers.size());
        for (var entry : readers.entrySet()) {
            values.put(entry.getKey(), entry.getValue().unpack(rs));
        }
        return mapper.assemble(type, values);
    }

    @Override
    public int columnCount() {
        var total = 0;
        for (var reader : readers.values()) {
            total += reader.columnCount();
        }
        return total;
    }
}
