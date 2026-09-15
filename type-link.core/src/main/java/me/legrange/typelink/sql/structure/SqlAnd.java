package me.legrange.typelink.sql.structure;

public record SqlAnd(SqlClause left, SqlClause right) implements SqlLogicalOperator {
}
