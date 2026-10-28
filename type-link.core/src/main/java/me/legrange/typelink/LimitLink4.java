package me.legrange.typelink;

final class LimitLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements Limit4<T1, T2, T3, T4>, LimitLink {

    private final int limit;
    private final int offset;

    LimitLink4(Link left, int limit, int offset) {
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
