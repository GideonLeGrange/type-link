package me.legrange.typelink.sql.structure;

public record SqlLe(SqlColumn left, SqlPart right) implements SqlSimpleOperator {
}
