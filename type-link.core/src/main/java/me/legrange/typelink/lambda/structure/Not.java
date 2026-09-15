package me.legrange.typelink.lambda.structure;

public record Not(Expression expression) implements Expression {

    @Override
    public Class<?> type() {
        return expression().type();
    }
}
