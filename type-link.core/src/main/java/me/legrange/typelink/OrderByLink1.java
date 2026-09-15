package me.legrange.typelink;

final class OrderByLink1<T1> extends Link1<T1> implements OrderBy1<T1>, OrderByLink {

    private final boolean reverse;

    public OrderByLink1(Link left, SelectFunction1<T1, ?> function, boolean reverse) {
        super(left, function);
        this.reverse = reverse;
    }

    @Override
    public boolean reversed() {
        return reverse;
    }

}
