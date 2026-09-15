package me.legrange.typelink.sql.structure;

public record SqlNotInSet(SqlColumn left, SqlPart right) implements SqlSetOperator {
}
