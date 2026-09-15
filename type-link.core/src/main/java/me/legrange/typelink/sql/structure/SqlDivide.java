package me.legrange.typelink.sql.structure;

public record SqlDivide(SqlColumn left, SqlColumn right) implements SqlOperation {
}
