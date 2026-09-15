package me.legrange.typelink;

public final class HavingLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements Having3<T1, T2, T3>, HavingLink {

    HavingLink3(Link left, QueryPredicate3<T1, T2, T3> predicate) {
        super(left, predicate);
    }
}

