package me.legrange.typelink.lambda.structure;

import java.util.List;

public record ListValue(List<Value> values) implements Value {
    @Override
    public Class<?> type() {
        return values().isEmpty() ? Object.class : values().getFirst().type();
    }
}
