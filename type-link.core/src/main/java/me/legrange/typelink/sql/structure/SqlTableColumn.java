package me.legrange.typelink.sql.structure;

/**
 * A column of a table. {@code type} is what the column holds; {@code optional} says the lambda saw it through an
 * {@link java.util.Optional}, so a projected value is wrapped in one on the way out.
 */
public record SqlTableColumn(String tableName, String name, Class<?> type, boolean optional) implements SqlColumn {

    public SqlTableColumn(String tableName, String name, Class<?> type) {
        this(tableName, name, type, false);
    }
}
