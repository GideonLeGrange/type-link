package me.legrange.typelink;

final class WhereLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements Where3<T1, T2, T3>, WhereLink {


    public WhereLink3(Link left, QueryPredicate3<T1, T2, T3> clause) {
        super(left, clause);
    }

}
