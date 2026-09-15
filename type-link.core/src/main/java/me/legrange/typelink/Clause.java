package me.legrange.typelink;

public sealed interface Clause permits
        Limit1, Limit2, Limit3,
        AndLink, DistinctLink, FromLink, GroupByLink, JoinLink, LimitLink, OrLink, OrderByLink, WhereLink {
}
