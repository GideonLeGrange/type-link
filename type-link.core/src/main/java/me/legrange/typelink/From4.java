package me.legrange.typelink;

public sealed interface From4<T1, T2, T3, T4> extends Limit4<T1, T2, T3, T4> permits FromLink4 {

    Where4<T1, T2, T3, T4> where(QueryPredicate4<T1, T2, T3, T4> clause);

    GroupBy4<T1, T2, T3, T4> groupBy(SelectFunction4<T1, T2, T3, T4, ?> function);

    GroupBy4<T1, T2, T3, T4> groupBy(SelectFunction4<T1, T2, T3, T4, ?> function1, SelectFunction4<T1, T2, T3, T4, ?> function2);

    GroupBy4<T1, T2, T3, T4> groupBy(SelectFunction4<T1, T2, T3, T4, ?> function1, SelectFunction4<T1, T2, T3, T4, ?> function2, SelectFunction4<T1, T2, T3, T4, ?> function3);

    OrderBy4<T1, T2, T3, T4> orderBy(SelectFunction4<T1, T2, T3, T4, ?> function1);

    OrderBy4<T1, T2, T3, T4> orderByDescending(SelectFunction4<T1, T2, T3, T4, ?> function1);

    Limit4<T1, T2, T3, T4> limit(int limit);

    Limit4<T1, T2, T3, T4> limit(int limit, int offset);

    Distinct4<T1, T2, T3, T4> distinct();

}
