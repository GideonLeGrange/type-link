package me.legrange.typelink.lambda.structure;

public sealed interface BinaryOperator extends Operator permits Add, Divide, Multiply, Subtract {

    Value left();

    Value right();

}
