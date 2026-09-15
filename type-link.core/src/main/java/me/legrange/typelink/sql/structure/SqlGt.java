package me.legrange.typelink.sql.structure;

public record SqlGt(SqlColumn left, SqlPart right) implements SqlSimpleOperator {
}
