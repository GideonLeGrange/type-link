package me.legrange.typelink.lambda.structure;

public sealed interface SetFunction extends Evaluation permits InSet, NotInSet {

    Value left();

    Value right();

    default Class<?> type() {
        return left().type();
    }
}
