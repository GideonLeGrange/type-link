package me.legrange.typelink.sql.structure;

public record SqlOr(SqlClause left, SqlClause right) implements SqlLogicalOperator {
}
