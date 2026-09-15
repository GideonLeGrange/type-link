package me.legrange.typelink.sql.structure;

public record SqlInnerJoin(SqlTableRef table, SqlClause on) implements SqlJoinClause {
}
