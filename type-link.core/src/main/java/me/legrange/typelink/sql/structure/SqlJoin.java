package me.legrange.typelink.sql.structure;

import java.util.List;

public record SqlJoin(List<SqlJoinClause> joins) {
}
