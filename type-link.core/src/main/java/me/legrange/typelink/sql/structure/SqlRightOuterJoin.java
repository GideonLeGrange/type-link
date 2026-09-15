package me.legrange.typelink.sql.structure;

public record SqlRightOuterJoin(SqlTableRef table, SqlClause on) implements SqlJoinClause {
}
