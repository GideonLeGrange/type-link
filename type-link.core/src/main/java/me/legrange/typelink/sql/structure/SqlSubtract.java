package me.legrange.typelink.sql.structure;

public record SqlSubtract(SqlColumn left, SqlColumn right) implements SqlOperation {
}
