package me.legrange.typelink;

public sealed interface OrLink extends Clause permits OrClause, OrLink1, OrLink2, OrLink3 {
}

