package me.legrange.typelink;

final class ThenOrderByLink1<T1> extends Link1<T1> implements OrderByLink, OrderBy1<T1> {

    private final boolean reverse;

    public ThenOrderByLink1(Link left, SelectFunction1<T1, ?> function, boolean reverse) {
        super(left, function);
        this.reverse = reverse;
    }

    @Override
    public boolean reversed() {
        return reverse;
    }
}
