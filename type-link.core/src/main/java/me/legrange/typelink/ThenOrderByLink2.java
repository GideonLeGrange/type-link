package me.legrange.typelink;

final class ThenOrderByLink2<T1, T2> extends Link2<T1, T2> implements OrderByLink, OrderBy2<T1, T2> {

    private final boolean reverse;

    public ThenOrderByLink2(Link left, SelectFunction2<T1, T2, ?> function, boolean reverse) {
        super(left, function);
        this.reverse = reverse;
    }

    @Override
    public boolean reversed() {
        return reverse;
    }
}
