package me.legrange.typelink.sql.structure;

import java.util.List;

public record SqlWhere(List<SqlClause> clauses) {
}
