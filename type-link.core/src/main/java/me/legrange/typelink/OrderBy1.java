package me.legrange.typelink;

public sealed interface OrderBy1<T1> extends Limit1<T1> permits  OrderByLink1, ThenOrderByLink1 {

    OrderBy1<T1> thenBy(SelectFunction1<T1, ?> function);

    OrderBy1<T1> thenByDescending(SelectFunction1<T1, ?> function);

    Limit1<T1> limit(int limit);

    Limit1<T1> limit(int limit, int offset);

}
