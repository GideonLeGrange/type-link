package me.legrange.typelink.sql.structure;

public record SqlGe(SqlColumn left, SqlPart right) implements SqlSimpleOperator {
}
