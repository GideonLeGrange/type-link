package me.legrange.typelink;

final class AndLink1<T1> extends Link1<T1> implements And1<T1>, AndLink {

    public AndLink1(Link left, QueryPredicate1<T1> clause) {
        super(left, clause);
    }

}
