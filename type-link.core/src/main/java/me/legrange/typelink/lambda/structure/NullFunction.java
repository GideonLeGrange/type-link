package me.legrange.typelink.lambda.structure;

public sealed interface NullFunction extends Evaluation permits IsNotNull, IsNull {

    Value left();

    @Override
    default Class<?> type() {
        return Boolean.class;
    }
}
