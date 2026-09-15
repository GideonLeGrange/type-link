package me.legrange.typelink.sql.structure;

public record SqlLeftOuterJoin(SqlTableRef table, SqlClause on) implements SqlJoinClause {
}
