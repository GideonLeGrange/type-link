package me.legrange.typelink.sql.structure;

import java.util.List;

public record SqlFrom(List<SqlTableRef> tables) {

    /** The types the query selects from, in order. */
    public List<Class<?>> types() {
        return tables.stream().map(SqlTableRef::type).toList();
    }
}
