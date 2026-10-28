package me.legrange.typelink;

final class AndLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements And4<T1, T2, T3, T4>, AndLink {

    public AndLink4(Link left, QueryPredicate4<T1, T2, T3, T4> clause) {
        super(left, clause);
    }

}

