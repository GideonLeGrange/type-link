package me.legrange.typelink;

final class DistinctLink1<T1> extends Link1<T1> implements Distinct1<T1>, DistinctLink {

    DistinctLink1(Link left) {
        super(left, null);
    }

}
