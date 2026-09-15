package me.legrange.typelink.sql.structure;

public sealed interface SqlRelationalOperator extends SqlClause permits SqlSimpleOperator, SqlLikeOperator,
        SqlSetOperator {

    SqlPart left();

    SqlPart right();
}
