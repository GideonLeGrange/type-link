package me.legrange.typelink.lambda.structure;

public record Max(Value left) implements AggregationOperator {
    @Override
    public Class<?> type() {
        return left.type();
    }
}
