package me.legrange.typelink;

public sealed interface Join2<T1, T2> extends Where2<T1, T2> permits JoinLink2 {

    Where2<T1, T2> where(QueryPredicate2<T1, T2> clause);

    <T3> Join3<T1, T2, T3> join(Class<T3> type, QueryPredicate3<T1, T2, T3> join);

    <T3> Join3<T1, T2, T3> leftJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join);

    <T3> Join3<T1, T2, T3> rightJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join);

    <T3> Join3<T1, T2, T3> fullJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join);

}
