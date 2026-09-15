package database.testing;

import bean.Bean;
import me.legrange.typelink.BeanMapper;
import me.legrange.typelink.TableMapper;
import me.legrange.typelink.sql.structure.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static me.legrange.typelink.sql.unpack.Readers.makeObjectReader;

final class ResultSetsX {

    private ResultSetsX() {
    }

    public static <T> List<T> getObjects(ResultSet rs, Class<T> type) throws SQLException {
        if (type.isRecord()) {
            return makeRecords(rs, type);
        }
        if (Bean.class.isAssignableFrom(type)) {
            return (List<T>) makeBeans(rs, (Class<Bean>) type);
        }
        throw new IllegalArgumentException("type must be a record or bean");
    }

    private static <T> List<T> makeRecords(ResultSet rs, Class<T> type) throws SQLException {
        var res = new ArrayList<T>();
        while (rs.next()) {
            res.add(makeRecord(rs, type));
        }
        return res;
    }

    private static <T extends Bean> List<T> makeBeans(ResultSet rs, Class<T> type) throws SQLException {
        var res = new ArrayList<T>();
        var reader = makeObjectReader(new BeanMapper(Bean.class), rs, type);
        while (rs.next()) {
            //noinspection unchecked
            res.add((T) reader.unpack(rs));
        }
        return res;
    }

    static <T> T makeRecord(ResultSet rs, Class<T> type) throws SQLException {
        var reader = makeObjectReader(TableMapper.RECORD_MAPPER, rs, type);
        //noinspection unchecked
        return (T) reader.unpack(rs);
    }

    private static Class<?> typeFor(SqlColumn column) {
        return switch (column) {
            case SqlAll _ -> null;
            case SqlConstant sqlConstant -> sqlConstant.value().getClass();
            case SqlFunction sqlFunction -> typeFor(sqlFunction.parameter());
            case SqlOperation sqlOperation -> typeFor(sqlOperation);
            case SqlTable sqlTable -> sqlTable.type();
            case SqlTableColumn sqlTableColumn -> sqlTableColumn.type();
            case SqlSubSelect subSelect -> typeFor(subSelect.select().select().columns());
            case SqlConcat _ -> String.class;
        };
    }

    private static Class<?> typeFor(SqlOperation column) {
        return promote(column.left(), column.right());
    }

    private static Class<?> promote(SqlColumn left, SqlColumn right) {
        return promoteTypes(typeFor(left), typeFor(right));
    }

    private static Class<?> promoteTypes(Class<?> left, Class<?> right) {
        if (Double.class.equals(left) || Double.class.equals(right) || Double.TYPE.equals(left) || Double.TYPE.equals(right)) {
            return Double.class;
        }
        if (Float.class.equals(left) || Float.class.equals(right) || Float.TYPE.equals(left) || Float.TYPE.equals(right)) {
            return Float.class;
        }
        if (Long.class.equals(left) || Long.class.equals(right) || Long.TYPE.equals(left) || Long.TYPE.equals(right)) {
            return Float.class;
        }
        return Integer.class;
    }
}
