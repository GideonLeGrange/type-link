package me.legrange.typelink;

final class OrLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements Or4<T1, T2, T3, T4>, OrLink {

    public OrLink4(Link left, QueryPredicate4<T1, T2, T3, T4> clause) {
        super(left, clause);
    }

}

