package me.legrange.typelink;

@FunctionalInterface
public non-sealed interface QueryPredicate4<T1, T2, T3, T4> extends QueryPredicate {

    boolean test(T1 t1, T2 t2, T3 t3, T4 t4);

}
