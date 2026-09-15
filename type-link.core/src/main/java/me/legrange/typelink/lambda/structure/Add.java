package me.legrange.typelink.lambda.structure;

public record Add(Class<?> type, Value left, Value right) implements BinaryOperator {

}
