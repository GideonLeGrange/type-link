package me.legrange.typelink;

final class LimitLink1<T1> extends Link1<T1> implements Limit1<T1>, LimitLink {

    private final int limit;
    private final int offset;

    LimitLink1(Link left, int limit, int offset) {
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
