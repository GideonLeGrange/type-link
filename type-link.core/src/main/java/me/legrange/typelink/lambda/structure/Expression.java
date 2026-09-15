package me.legrange.typelink.lambda.structure;

public sealed interface Expression extends Value permits LogicalOperator,
        Not, Evaluation {
}
