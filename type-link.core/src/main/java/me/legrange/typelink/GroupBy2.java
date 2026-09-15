package me.legrange.typelink;

public sealed interface GroupBy2<T1, T2> extends Limit2<T1, T2> permits GroupByLink2, Having2 {

    OrderBy2<T1, T2> orderBy(SelectFunction2<T1, T2, ?> function);

    OrderBy2<T1, T2> orderByDescending(SelectFunction2<T1, T2, ?> function1);

    Having2<T1, T2> having(QueryPredicate2<T1, T2> predicate);

    Limit2<T1, T2> limit(int limit);

    Limit2<T1, T2> limit(int limit, int offset);

}
