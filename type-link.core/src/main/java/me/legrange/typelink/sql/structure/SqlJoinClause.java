package me.legrange.typelink.sql.structure;

public sealed interface SqlJoinClause permits SqlFullOuterJoin, SqlInnerJoin, SqlLeftOuterJoin, SqlRightOuterJoin {

    SqlTableRef table();

    SqlClause on();

    /** The joined type. */
    default Class<?> type() {
        return table().type();
    }
}
