package me.legrange.typelink.lambda.structure;

public record Min(Value left) implements AggregationOperator {
    @Override
    public Class<?> type() {
        return left.type();
    }
}
