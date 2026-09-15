package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Expression;
import me.legrange.typelink.lambda.structure.Lambda;

import java.util.List;

public final class JoinClause extends Link implements JoinLink {

    private final Class<?> type;
    private final Type joinType;

    public JoinClause(Link left, Class<?> type, Type joinType, Expression expression) {
        super(left, new Lambda(expression, List.of()));
        this.type = type;
        this.joinType = joinType;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Type joinType() {
        return joinType;
    }
}
