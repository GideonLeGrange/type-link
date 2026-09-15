package me.legrange.typelink.lambda.structure;

public record Subtract(Class<?> type, Value left, Value right) implements BinaryOperator {

}
