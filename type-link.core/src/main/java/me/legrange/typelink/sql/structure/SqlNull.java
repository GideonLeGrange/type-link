package me.legrange.typelink.sql.structure;

public sealed interface SqlNull extends SqlClause permits SqlIsNotNull, SqlIsNull {
    SqlColumn column();
}
