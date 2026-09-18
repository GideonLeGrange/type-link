package me.legrange.typelink;


public sealed interface From1<T1> extends Limit1<T1> permits FromLink1 {

    Where1<T1> where(QueryPredicate1<T1> clause);

    GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function);

    GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function1, SelectFunction1<T1, ?> function2);

    GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function1, SelectFunction1<T1, ?> function2, SelectFunction1<T1, ?> function3);

    OrderBy1<T1> orderBy(SelectFunction1<T1, ?> function1);

    OrderBy1<T1> orderByDescending(SelectFunction1<T1, ?> function1);

    Limit1<T1> limit(int limit);

    Limit1<T1> limit(int limit, int offset);

    Distinct1<T1> distinct();

    <T2> Join2<T1, T2> join(Class<T2> type, QueryPredicate2<T1, T2> join);

    <T2> Join2<T1, T2> leftJoin(Class<T2> type, QueryPredicate2<T1, T2> join);

    <T2> Join2<T1, T2> rightJoin(Class<T2> type, QueryPredicate2<T1, T2> join);

    <T2> Join2<T1, T2> fullJoin(Class<T2> type, QueryPredicate2<T1, T2> join);

}
