package me.legrange.typelink.lambda.structure;

public record Negate(Class<?> type, Value value) implements UnaryOperator {
}
