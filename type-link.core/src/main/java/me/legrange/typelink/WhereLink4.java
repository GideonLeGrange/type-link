package me.legrange.typelink;

final class WhereLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements Where4<T1, T2, T3, T4>, WhereLink {


    public WhereLink4(Link left, QueryPredicate4<T1, T2, T3, T4> clause) {
        super(left, clause);
    }

}
