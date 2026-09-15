package me.legrange.typelink.sql.structure;

public record SqlTableColumn(String tableName, String name, Class<?> type) implements SqlColumn {
}
