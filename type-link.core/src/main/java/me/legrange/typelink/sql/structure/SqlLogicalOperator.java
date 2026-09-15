package me.legrange.typelink.sql.structure;

public sealed interface SqlLogicalOperator extends SqlClause permits SqlAnd, SqlOr {

    SqlClause left();

    SqlClause right();

}
