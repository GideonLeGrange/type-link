package me.legrange.typelink;

public sealed interface Where3<T1, T2, T3> extends Limit3<T1, T2, T3> permits WhereLink3 {

    And3<T1, T2, T3> and(QueryPredicate3<T1, T2, T3> clause);

    Or3<T1, T2, T3> or(QueryPredicate3<T1, T2, T3> clause);

    GroupBy3<T1, T2, T3> groupBy(SelectFunction3<T1, T2, T3, ?> function);

    GroupBy3<T1, T2, T3> groupBy(SelectFunction3<T1, T2, T3, ?> function1, SelectFunction3<T1, T2, T3, ?> function2);

    GroupBy3<T1, T2, T3> groupBy(SelectFunction3<T1, T2, T3, ?> function1, SelectFunction3<T1, T2, T3, ?> function2, SelectFunction3<T1, T2, T3, ?> function3);

    OrderBy3<T1, T2, T3> orderBy(SelectFunction3<T1, T2, T3, ?> function1);

    OrderBy3<T1, T2, T3> orderByDescending(SelectFunction3<T1, T2, T3, ?> function1);

    Limit3<T1, T2, T3> limit(int limit);

    Limit3<T1, T2, T3> limit(int limit, int offset);

    Distinct3<T1, T2, T3> distinct();

}
