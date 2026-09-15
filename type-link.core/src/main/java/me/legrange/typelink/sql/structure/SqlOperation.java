package me.legrange.typelink.sql.structure;

public sealed interface SqlOperation extends SqlColumn permits  SqlAdd, SqlDivide, SqlMultiply, SqlSubtract {
    SqlColumn left();

    SqlColumn right();

}
