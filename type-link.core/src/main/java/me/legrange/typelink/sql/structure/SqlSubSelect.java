package me.legrange.typelink.sql.structure;

public record SqlSubSelect(SqlQuery select) implements SqlClause, SqlColumn {
}
