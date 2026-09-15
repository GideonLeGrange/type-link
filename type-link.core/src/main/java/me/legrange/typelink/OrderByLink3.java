package me.legrange.typelink;

final class OrderByLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements OrderBy3<T1, T2, T3>, OrderByLink {

    private final boolean reverse;

    public OrderByLink3(Link left, SelectFunction3<T1, T2, T3, ?> function, boolean reverse) {
        super(left, function);
        this.reverse = reverse;
    }


    @Override
    public boolean reversed() {
        return reverse;
    }

}
