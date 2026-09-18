package me.legrange.typelink;


public sealed interface From2<T1, T2> extends Limit2<T1, T2> permits FromLink2 {

    Where2<T1, T2> where(QueryPredicate2<T1, T2> clause);

    GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function);

    GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function1, SelectFunction2<T1, T2, ?> function2);

    GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function1, SelectFunction2<T1, T2, ?> function2, SelectFunction2<T1, T2, ?> function3);

    OrderBy2<T1, T2> orderBy(SelectFunction2<T1, T2, ?> function);

    OrderBy2<T1, T2> orderByDescending(SelectFunction2<T1, T2, ?> function1);

    Limit2<T1, T2> limit(int limit);

    Limit2<T1, T2> limit(int limit, int offset);

    Distinct2<T1, T2> distinct();

    <T3> Join3<T1, T2, T3> join(Class<T3> type, QueryPredicate3<T1, T2, T3> join);

    <T3> Join3<T1, T2, T3> leftJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join);

    <T3> Join3<T1, T2, T3> rightJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join);

    <T3> Join3<T1, T2, T3> fullJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join);


}
