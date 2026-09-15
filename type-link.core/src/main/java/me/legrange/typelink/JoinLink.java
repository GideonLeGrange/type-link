package me.legrange.typelink;

public sealed interface JoinLink extends Clause permits JoinClause, JoinLink2, JoinLink3 {

    Class<?> type();

    Type joinType();

    enum Type {
        INNER,
        LEFT_OUTER,
        RIGHT_OUTER,
        FULL_OUTER
    }

}
