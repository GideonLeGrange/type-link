package me.legrange.typelink;

public final class HavingLink1<T1> extends Link1<T1> implements Having1<T1>, HavingLink {

    HavingLink1(Link left, QueryPredicate1<T1> predicate) {
        super(left, predicate);
    }
}
