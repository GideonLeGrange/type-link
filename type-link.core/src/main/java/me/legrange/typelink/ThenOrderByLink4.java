package me.legrange.typelink;

final class ThenOrderByLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements OrderByLink, OrderBy4<T1, T2, T3, T4> {

    private final boolean reverse;

    public ThenOrderByLink4(Link left, SelectFunction4<T1, T2, T3, T4, ?> function, boolean reverse) {
        super(left, function);
        this.reverse = reverse;
    }

    @Override
    public boolean reversed() {
        return reverse;
    }
}
