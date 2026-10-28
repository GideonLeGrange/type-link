package me.legrange.typelink;

public sealed interface Distinct4<T1, T2, T3, T4> extends Limit4<T1, T2, T3, T4> permits DistinctLink4 {

    OrderBy4<T1, T2, T3, T4> orderBy(SelectFunction4<T1, T2, T3, T4, ?> function1);

    OrderBy4<T1, T2, T3, T4> orderByDescending(SelectFunction4<T1, T2, T3, T4, ?> function1);

    Limit4<T1, T2, T3, T4> limit(int limit);

    Limit4<T1, T2, T3, T4> limit(int limit, int offset);

}
