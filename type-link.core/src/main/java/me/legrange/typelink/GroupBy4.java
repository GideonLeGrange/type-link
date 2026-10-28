package me.legrange.typelink;

public sealed interface GroupBy4<T1, T2, T3, T4> extends Limit4<T1, T2, T3, T4> permits GroupByLink4, Having4 {

    OrderBy4<T1, T2, T3, T4> orderBy(SelectFunction4<T1, T2, T3, T4, ?> function1);

    OrderBy4<T1, T2, T3, T4> orderByDescending(SelectFunction4<T1, T2, T3, T4, ?> function1);

    Having4<T1, T2, T3, T4> having(QueryPredicate4<T1, T2, T3, T4> predicate);

    Limit4<T1, T2, T3, T4> limit(int limit);

    Limit4<T1, T2, T3, T4> limit(int limit, int offset);

}
