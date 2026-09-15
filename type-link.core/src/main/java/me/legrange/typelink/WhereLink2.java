package me.legrange.typelink;

final class WhereLink2<T1, T2> extends Link2<T1, T2> implements Where2<T1, T2>, WhereLink {

    public WhereLink2(Link left, QueryPredicate2<T1, T2> clause) {
        super(left, clause);
    }


}
