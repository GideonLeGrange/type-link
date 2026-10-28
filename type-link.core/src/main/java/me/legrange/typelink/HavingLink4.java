package me.legrange.typelink;

public final class HavingLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements Having4<T1, T2, T3, T4>, HavingLink {

    HavingLink4(Link left, QueryPredicate4<T1, T2, T3, T4> predicate) {
        super(left, predicate);
    }
}

