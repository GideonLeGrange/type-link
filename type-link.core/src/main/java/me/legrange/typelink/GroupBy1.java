package me.legrange.typelink;

public sealed interface GroupBy1<T1> extends Limit1<T1> permits GroupByLink1, Having1{

    OrderBy1<T1> orderBy(SelectFunction1<T1, ?> function1);

    OrderBy1<T1> orderByDescending(SelectFunction1<T1, ?> function1);

    Having1<T1> having(QueryPredicate1<T1> predicate);

    Limit1<T1> limit(int limit);

    Limit1<T1> limit(int limit, int offset);


}
