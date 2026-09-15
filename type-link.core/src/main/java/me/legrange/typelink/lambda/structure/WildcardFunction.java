package me.legrange.typelink.lambda.structure;

public sealed interface WildcardFunction extends Evaluation permits EndsWith, NotEndsWith,
        NotStartsWith, StartsWith, Contains, NotContains {

    Value left();

    Value right();

    @Override
    default Class<?> type() {
        return left().type();
    }

}
