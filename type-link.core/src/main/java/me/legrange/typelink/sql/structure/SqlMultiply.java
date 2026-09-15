package me.legrange.typelink.sql.structure;

public record SqlMultiply(SqlColumn left, SqlColumn right) implements SqlOperation {
}
