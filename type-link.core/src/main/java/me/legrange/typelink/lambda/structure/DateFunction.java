package me.legrange.typelink.lambda.structure;

public sealed interface DateFunction extends Evaluation permits IsAfter, IsBefore {

    Value right();

    default Class<?> type() {
        return left().type();
    }
}
