package me.legrange.typelink;

public sealed interface Where1<T1> extends Limit1<T1> permits From1, WhereLink1 {

    And1<T1> and(QueryPredicate1<T1> clause);

    Or1<T1> or(QueryPredicate1<T1> clause);

    GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function);

    GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function1, SelectFunction1<T1, ?> function2);

    GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function1, SelectFunction1<T1, ?> function2, SelectFunction1<T1, ?> function3);

    OrderBy1<T1> orderBy(SelectFunction1<T1, ?> function1);

    OrderBy1<T1> orderByDescending(SelectFunction1<T1, ?> function1);

    Limit1<T1> limit(int limit);

    Limit1<T1> limit(int limit, int offset);

    Distinct1<T1> distinct();

}
