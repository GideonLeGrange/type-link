package me.legrange.typelink.lambda.structure;

public sealed interface Evaluation extends Expression permits RelationalEvaluation,
        WildcardFunction, NullFunction, SetFunction, DateFunction {

    Value left();
}
