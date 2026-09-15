package me.legrange.typelink;

public sealed interface OrderBy2<T1, T2> extends Limit2<T1, T2> permits OrderByLink2, ThenOrderByLink2 {

    OrderBy2<T1, T2> thenBy(SelectFunction2<T1, T2, ?> function);

    OrderBy2<T1, T2> thenByDescending(SelectFunction2<T1, T2, ?> function);

    Limit2<T1, T2> limit(int limit);

    Limit2<T1, T2> limit(int limit, int offset);

}
