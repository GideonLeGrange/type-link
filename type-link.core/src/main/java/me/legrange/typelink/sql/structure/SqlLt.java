package me.legrange.typelink.sql.structure;

public record SqlLt(SqlColumn left, SqlPart right) implements SqlSimpleOperator {
}
