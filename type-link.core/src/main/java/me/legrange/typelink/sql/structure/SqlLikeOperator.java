package me.legrange.typelink.sql.structure;

public abstract sealed class SqlLikeOperator implements SqlRelationalOperator permits SqlLike, SqlNotLike {

    private final SqlColumn left;
    private final SqlPart right;
    private final Wildcard wildcard;

    public SqlLikeOperator(SqlColumn left, SqlPart right, Wildcard wildcard) {
        this.left = left;
        this.right = right;
        this.wildcard = wildcard;
    }

    public SqlColumn left() {
        return left;
    }

    public SqlPart right() {
        return right;
    }

    public Wildcard wildcard() {
        return wildcard;
    }

    public enum Wildcard {
        LEFT, RIGHT, BOTH
    }
}
