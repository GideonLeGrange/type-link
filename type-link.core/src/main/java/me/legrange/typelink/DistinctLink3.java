package me.legrange.typelink;

final class DistinctLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements Distinct3<T1, T2, T3>, DistinctLink {

    DistinctLink3(Link left) {
        super(left, null);
    }

}
