package me.legrange.typelink.sql.structure;

public sealed interface SqlSimpleOperator extends SqlRelationalOperator permits SqlEq, SqlGe, SqlGt,
        SqlLe, SqlLt, SqlNeq {
}
