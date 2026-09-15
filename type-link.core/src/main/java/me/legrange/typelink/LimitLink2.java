package me.legrange.typelink;

final class LimitLink2<T1, T2> extends Link2<T1, T2> implements Limit2<T1, T2>, LimitLink {

    private final int limit;
    private final int offset;

    LimitLink2(Link left, int limit, int offset) {
        super(left, null);
        this.limit = limit;
        this.offset = offset;
    }

    public int limit() {
        return limit;
    }

    public int offset() {
        return offset;
    }

}
