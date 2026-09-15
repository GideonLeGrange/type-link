package me.legrange.typelink.sql.structure;

public record SqlAdd(SqlColumn left, SqlColumn right) implements  SqlOperation {
}
