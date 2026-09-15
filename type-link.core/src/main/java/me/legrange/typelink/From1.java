package me.legrange.typelink;


public sealed interface From1<T1> extends Where1<T1> permits FromLink1 {

    Where1<T1> where(QueryPredicate1<T1> clause);

    <T2> Join2<T1, T2> join(Class<T2> type, QueryPredicate2<T1, T2> join);

    <T2> Join2<T1, T2> leftJoin(Class<T2> type, QueryPredicate2<T1, T2> join);

    <T2> Join2<T1, T2> rightJoin(Class<T2> type, QueryPredicate2<T1, T2> join);

    <T2> Join2<T1, T2> fullJoin(Class<T2> type, QueryPredicate2<T1, T2> join);

}
