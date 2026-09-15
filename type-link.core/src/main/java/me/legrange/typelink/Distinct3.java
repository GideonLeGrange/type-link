package me.legrange.typelink;

public sealed interface Distinct3<T1, T2, T3> extends Limit3<T1, T2, T3> permits DistinctLink3 {

    OrderBy3<T1, T2, T3> orderBy(SelectFunction3<T1, T2, T3, ?> function1);

    OrderBy3<T1, T2, T3> orderByDescending(SelectFunction3<T1, T2, T3, ?> function1);

    Limit3<T1, T2, T3> limit(int limit);

    Limit3<T1, T2, T3> limit(int limit, int offset);

}
