package me.legrange.typelink.sql.structure;

public sealed interface SqlPart permits SqlColumn, SqlValue, SqlClause {
}
