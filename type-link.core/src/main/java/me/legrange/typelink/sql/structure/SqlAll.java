package me.legrange.typelink.sql.structure;

import java.util.List;

public record SqlAll(List<SqlColumn> columns) implements SqlColumn {
}
