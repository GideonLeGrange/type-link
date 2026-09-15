package me.legrange.typelink.sql.structure;

public record SqlFullOuterJoin(SqlTableRef table, SqlClause on) implements SqlJoinClause {
}
