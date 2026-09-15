package me.legrange.typelink;

public sealed interface Distinct1<T1> extends Limit1<T1> permits DistinctLink1 {

    OrderBy1<T1> orderBy(SelectFunction1<T1, ?> function1);

    OrderBy1<T1> orderByDescending(SelectFunction1<T1, ?> function1);

    Limit1<T1> limit(int limit);

    Limit1<T1> limit(int limit, int offset);

}
