package me.legrange.typelink;

public sealed interface Join3<T1, T2, T3> extends Where3<T1, T2, T3> permits JoinLink3 {

    Where3<T1, T2, T3> where(QueryPredicate3<T1, T2, T3> clause);

}
