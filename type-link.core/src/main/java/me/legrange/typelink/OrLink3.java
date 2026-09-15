package me.legrange.typelink;

final class OrLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements Or3<T1, T2, T3>, OrLink {

    public OrLink3(Link left, QueryPredicate3<T1, T2, T3> clause) {
        super(left, clause);
    }

}

