package me.legrange.typelink.lambda.structure;

public sealed interface AggregationOperator extends Operator permits Avg, Count, Max, Min, Sum {

    Value left();

}
