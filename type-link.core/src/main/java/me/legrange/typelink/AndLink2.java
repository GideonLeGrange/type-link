package me.legrange.typelink;

final class AndLink2<T1, T2> extends Link2<T1, T2> implements And2<T1, T2>, AndLink {

    public AndLink2(Link left, QueryPredicate2<T1, T2> clause) {
        super(left, clause);
    }

}

