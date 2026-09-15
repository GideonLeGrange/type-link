package me.legrange.typelink;

public sealed interface AndLink extends Clause permits AndClause, AndLink1, AndLink2, AndLink3 {
}
