package me.legrange.typelink.lambda.structure;

import static java.lang.String.format;

public record And(Expression left, Expression right) implements LogicalOperator {

    @Override
    public String toString() {
        return format("(%s AND %s)", left, right);
    }
}
