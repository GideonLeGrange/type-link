package me.legrange.typelink;

public sealed interface And2<T1, T2> extends Limit2<T1, T2> permits AndLink2 {

    And2<T1, T2> and(QueryPredicate2<T1, T2> clause);

    GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function);

    GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function1, SelectFunction2<T1, T2, ?> function2);

    GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function1, SelectFunction2<T1, T2, ?> function2, SelectFunction2<T1, T2, ?> function3);

    OrderBy2<T1, T2> orderBy(SelectFunction2<T1, T2, ?> function);

    OrderBy2<T1, T2> orderByDescending(SelectFunction2<T1, T2, ?> function1);

    Limit2<T1, T2> limit(int limit);

    Limit2<T1, T2> limit(int limit, int offset);

    Distinct2<T1, T2> distinct();
}

