package me.legrange.typelink.lambda.structure;

public record Multiply(Class<?> type, Value left, Value right) implements BinaryOperator {

}
