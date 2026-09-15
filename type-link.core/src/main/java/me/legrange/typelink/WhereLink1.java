package me.legrange.typelink;

final class WhereLink1<T1> extends Link1<T1> implements Where1<T1>, WhereLink {

    public WhereLink1(Link left, QueryPredicate1<T1> clause) {
        super(left, clause);
    }

}
