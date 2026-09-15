package me.legrange.typelink.lambda.structure;

public sealed interface Operator extends Value permits BinaryOperator, AggregationOperator, FunctionOperator {

}
