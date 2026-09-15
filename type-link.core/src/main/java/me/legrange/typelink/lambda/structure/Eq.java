package me.legrange.typelink.lambda.structure;

import static java.lang.String.format;

public record Eq(Value left, Value right) implements RelationalEvaluation {

    public String toString() {
        return format("%s == %s", left, right);
    }

}
