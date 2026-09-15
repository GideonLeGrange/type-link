package me.legrange.typelink.sql.structure;

public sealed interface SqlValue extends SqlPart permits SqlList, SqlConstant {
}
