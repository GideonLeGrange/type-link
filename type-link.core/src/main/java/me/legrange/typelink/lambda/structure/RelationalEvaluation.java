package me.legrange.typelink.lambda.structure;

public sealed interface RelationalEvaluation extends Evaluation permits Eq, Neq, Ge, Gt, Le, Lt {

    Value left();

    Value right();

    @Override
    default Class<?> type() {
        return left().type();
    }

}
