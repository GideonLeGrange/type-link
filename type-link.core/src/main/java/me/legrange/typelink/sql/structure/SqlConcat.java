package me.legrange.typelink.sql.structure;

import java.util.List;

public record SqlConcat(List<SqlColumn> parameters) implements SqlColumn {
}
