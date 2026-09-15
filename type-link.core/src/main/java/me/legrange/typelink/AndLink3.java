package me.legrange.typelink;

final class AndLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements And3<T1, T2, T3>, AndLink {

    public AndLink3(Link left, QueryPredicate3<T1, T2, T3> clause) {
        super(left, clause);
    }

}

