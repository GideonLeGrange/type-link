package me.legrange.typelink;

@FunctionalInterface
public non-sealed interface QueryPredicate2<T1, T2> extends QueryPredicate {

    boolean test(T1 t1, T2 t2);

}
