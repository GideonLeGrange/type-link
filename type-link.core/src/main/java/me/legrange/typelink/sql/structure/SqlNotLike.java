package me.legrange.typelink.sql.structure;

public final class SqlNotLike extends SqlLikeOperator {

    public SqlNotLike(SqlColumn left, SqlPart right, Wildcard wildcard) {
        super(left, right, wildcard);
    }

}
