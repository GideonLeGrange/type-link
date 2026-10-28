package me.legrange.typelink;

final class OrderByLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements OrderBy4<T1, T2, T3, T4>, OrderByLink {

    private final boolean reverse;

    public OrderByLink4(Link left, SelectFunction4<T1, T2, T3, T4, ?> function, boolean reverse) {
        super(left, function);
        this.reverse = reverse;
    }


    @Override
    public boolean reversed() {
        return reverse;
    }

}
