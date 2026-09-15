package me.legrange.typelink.sql.structure;

public sealed interface SqlFunction extends SqlColumn permits SqlAvg, SqlCount, SqlMax, SqlMin, SqlSum {
    SqlColumn parameter();
}
