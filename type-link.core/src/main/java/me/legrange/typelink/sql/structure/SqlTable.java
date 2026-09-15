package me.legrange.typelink.sql.structure;

import java.util.List;

public record SqlTable(SqlTableRef table, List<SqlTableColumn> columns) implements SqlColumn {

    /** The type this table is mapped from. */
    public Class<?> type() {
        return table.type();
    }
}
