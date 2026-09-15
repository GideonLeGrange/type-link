package me.legrange.typelink.sql.structure;

public record SqlNeq(SqlColumn left, SqlPart right) implements SqlSimpleOperator {
}
