package me.legrange.typelink;

final class ThenOrderByLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements OrderByLink, OrderBy3<T1, T2, T3> {

    private final boolean reverse;

    public ThenOrderByLink3(Link left, SelectFunction3<T1, T2, T3, ?> function, boolean reverse) {
        super(left, function);
        this.reverse = reverse;
    }

    @Override
    public boolean reversed() {
        return reverse;
    }
}
