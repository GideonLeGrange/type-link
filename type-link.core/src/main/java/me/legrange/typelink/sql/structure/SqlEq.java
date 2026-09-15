package me.legrange.typelink.sql.structure;

public record SqlEq(SqlColumn left, SqlPart right) implements SqlSimpleOperator {
}
