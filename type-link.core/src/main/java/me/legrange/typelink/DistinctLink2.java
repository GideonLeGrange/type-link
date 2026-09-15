package me.legrange.typelink;

final class DistinctLink2<T1, T2> extends Link2<T1, T2> implements Distinct2<T1, T2>, DistinctLink {

    DistinctLink2(Link left) {
        super(left, null);
    }

}
