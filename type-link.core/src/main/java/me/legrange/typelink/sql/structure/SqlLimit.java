package me.legrange.typelink.sql.structure;

public sealed interface SqlLimit permits SqlLimited, SqlNotLimited {
}
