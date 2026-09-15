package me.legrange.typelink.lambda.structure;

import static java.lang.String.format;

public record Le(Value left, Value right) implements RelationalEvaluation {
    @Override
    public String toString() {
        return format("%s <= %s", left, right);
    }

}
