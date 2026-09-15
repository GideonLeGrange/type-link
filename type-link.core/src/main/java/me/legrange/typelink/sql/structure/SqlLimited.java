package me.legrange.typelink.sql.structure;

public record SqlLimited(int limit, int offset) implements SqlLimit {
}
