package me.legrange.typelink.sql.structure;

public sealed interface SqlColumn extends SqlPart permits SqlAll, SqlConstant, SqlFunction,
        SqlOperation, SqlSubSelect, SqlTable, SqlTableColumn, SqlConcat {
}
