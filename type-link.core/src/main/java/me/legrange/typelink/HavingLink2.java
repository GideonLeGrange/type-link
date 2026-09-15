package me.legrange.typelink;

public final class HavingLink2<T1, T2> extends Link2<T1, T2> implements Having2<T1, T2>, HavingLink {

    HavingLink2(Link left, QueryPredicate2<T1, T2> predicate) {
        super(left, predicate);
    }
}

