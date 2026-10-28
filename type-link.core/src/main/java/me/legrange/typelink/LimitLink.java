package me.legrange.typelink;

public sealed interface LimitLink extends Clause permits LimitClause, LimitLink1, LimitLink2, LimitLink3, LimitLink4 {

    int limit();

    int offset();

}
