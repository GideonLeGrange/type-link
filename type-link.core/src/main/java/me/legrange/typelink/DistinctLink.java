package me.legrange.typelink;

public sealed interface DistinctLink extends Clause permits DistinctClause, DistinctLink1, DistinctLink2, DistinctLink3 {
}
