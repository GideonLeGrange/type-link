package me.legrange.typelink;

final class OrLink2<T1, T2> extends Link2<T1, T2> implements Or2<T1, T2>, OrLink {

    public OrLink2(Link left, QueryPredicate2<T1, T2> clause) {
        super(left, clause);
    }

}

