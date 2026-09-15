package me.legrange.typelink.sql.unpack;

import me.legrange.typelink.sql.structure.*;

final class Types {

    private Types() {
    }

    public static Class<?> typeFor(SqlColumn column) {
        return switch (column) {
            case SqlAll _ -> throw new UnpackException("Can't determine type for *. BUG!");
            case SqlConstant sqlConstant -> sqlConstant.value().getClass();
            case SqlFunction sqlFunction -> typeFor(sqlFunction);
            case SqlOperation sqlOperation -> typeFor(sqlOperation);
            case SqlTable sqlTable -> sqlTable.type();
            case SqlTableColumn sqlTableColumn -> sqlTableColumn.type();
            case SqlSubSelect subSelect -> typeFor(subSelect.select().select().columns());
            case SqlConcat _ -> String.class;
        };
    }

    private static Class<?> typeFor(SqlFunction function) {
        return switch (function) {
            case SqlAvg _ -> Double.class;
            case SqlCount _ -> Long.class;
            case SqlMax sqlMax -> typeFor(sqlMax.parameter());
            case SqlMin sqlMin -> typeFor(sqlMin.parameter());
            case SqlSum sqlSum -> typeFor(sqlSum.parameter());
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
            return Long.class;
        }
        return Integer.class;
    }
}
