package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Lambda;

public final class LimitClause extends Link implements LimitLink {

    private final int limit;
    private final int offset;

    public LimitClause(Link left, int limit, int offset) {
        super(left, (Lambda) null);
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public int offset() {
        return offset;
    }
}
