package me.legrange.typelink.lambda.structure;

public record Count(Value left) implements AggregationOperator {
    @Override
    public Class<?> type() {
        return left.type();
    }
}
