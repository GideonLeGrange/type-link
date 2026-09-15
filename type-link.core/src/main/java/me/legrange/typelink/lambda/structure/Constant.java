package me.legrange.typelink.lambda.structure;

public record Constant<T>(T value) implements Value {

    public String toString() {
        return value != null ? value.toString() : "null";
    }

    @Override
    public Class<?> type() {
        return value.getClass();
    }
}
