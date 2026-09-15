package me.legrange.typelink;

@FunctionalInterface
public non-sealed interface QueryPredicate3<T1, T2, T3> extends QueryPredicate {

    boolean test(T1 t1, T2 t2, T3 t3);

}
