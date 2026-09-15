package me.legrange.typelink.lambda.structure;

import java.util.List;

public record Concat(List<Value> values) implements FunctionOperator {
    @Override
    public Class<?> type() {
        return String.class;
    }
}
