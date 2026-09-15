package me.legrange.typelink.sql.structure;

public record SqlInSet(SqlColumn left, SqlPart right) implements SqlSetOperator {
}
