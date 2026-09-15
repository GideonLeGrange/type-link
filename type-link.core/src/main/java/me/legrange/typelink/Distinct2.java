package me.legrange.typelink;

public sealed interface Distinct2<T1, T2> extends Limit2<T1, T2> permits DistinctLink2 {

    OrderBy2<T1, T2> orderBy(SelectFunction2<T1, T2, ?> function1);

    OrderBy2<T1, T2> orderByDescending(SelectFunction2<T1, T2, ?> function1);

    Limit2<T1, T2> limit(int limit);

    Limit2<T1, T2> limit(int limit, int offset);

}
