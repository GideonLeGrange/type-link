package me.legrange.typelink.sql.structure;

public record SqlConstant(Object value) implements SqlValue, SqlColumn {
}
