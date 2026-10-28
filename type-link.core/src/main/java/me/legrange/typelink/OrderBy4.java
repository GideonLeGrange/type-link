package me.legrange.typelink;

public sealed interface OrderBy4<T1, T2, T3, T4> extends Limit4<T1, T2, T3, T4> permits OrderByLink4, ThenOrderByLink4 {

    OrderBy4<T1, T2, T3, T4> thenBy(SelectFunction4<T1, T2, T3, T4, ?> function);

        OrderBy4<T1, T2, T3, T4> thenByDescending(SelectFunction4<T1, T2, T3, T4, ?> function);

    Limit4<T1, T2, T3, T4> limit(int limit);

    Limit4<T1, T2, T3, T4> limit(int limit, int offset);

}
