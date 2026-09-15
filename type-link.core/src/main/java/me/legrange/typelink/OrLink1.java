package me.legrange.typelink;

final class OrLink1<T1> extends Link1<T1> implements Or1<T1>, OrLink {

    public OrLink1(Link left, QueryPredicate1<T1> clause) {
        super(left, clause);
    }

}

