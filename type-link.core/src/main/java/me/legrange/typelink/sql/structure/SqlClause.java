package me.legrange.typelink.sql.structure;

public sealed interface SqlClause extends SqlPart permits SqlSubSelect, SqlLogicalOperator, SqlNot, SqlNull, SqlRelationalOperator {
}
