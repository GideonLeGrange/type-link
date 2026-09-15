package me.legrange.typelink.lambda.structure;

public sealed interface LogicalOperator extends Expression permits And, Or {

    Expression left();

    Expression right();

    default Class<?> type() {
        return left().type();
    }
}
