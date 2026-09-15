package me.legrange.typelink.sql.structure;

public record SqlQuery(SqlSelect select,
                       SqlFrom from,
                       SqlJoin joins,
                       SqlWhere where,
                       SqlGroupBy groupBy,
                       SqlHaving having,
                       SqlOrder order,
                       SqlLimit limit) {
}
