package me.legrange.typelink;

final class LimitLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements Limit3<T1, T2, T3>, LimitLink {

    private final int limit;
    private final int offset;

    LimitLink3(Link left, int limit, int offset) {
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
