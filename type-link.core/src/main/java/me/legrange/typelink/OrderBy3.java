package me.legrange.typelink;

public sealed interface OrderBy3<T1, T2, T3> extends Limit3<T1, T2, T3> permits OrderByLink3, ThenOrderByLink3 {

    OrderBy3<T1, T2, T3> thenBy(SelectFunction3<T1, T2, T3, ?> function);

        OrderBy3<T1, T2, T3> thenByDescending(SelectFunction3<T1, T2, T3, ?> function);

    Limit3<T1, T2, T3> limit(int limit);

    Limit3<T1, T2, T3> limit(int limit, int offset);

}
