package me.legrange.typelink;

final class OrderByLink2<T1, T2> extends Link2<T1, T2> implements OrderBy2<T1, T2>, OrderByLink {

    private final boolean reverse;

    public OrderByLink2(Link left, SelectFunction2<T1, T2, ?> function, boolean reverse) {
        super(left, function);
        this.reverse = reverse;
    }

    @Override
    public boolean reversed() {
        return reverse;
    }
}
