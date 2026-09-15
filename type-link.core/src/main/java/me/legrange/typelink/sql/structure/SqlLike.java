package me.legrange.typelink.sql.structure;

public final class SqlLike extends SqlLikeOperator {

    public SqlLike(SqlColumn left, SqlPart right, Wildcard wildcard) {
        super(left, right, wildcard);
    }
}
