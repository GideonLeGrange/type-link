package me.legrange.typelink.lambda.structure;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.stream.Collectors;

public record ConstructorCall(Class<?> type, Constructor<?> constructor, List<Value> parameters) implements Call {

    public String toString() {
        return "new " + type.getSimpleName() + "(" +
                parameters.stream().map(Object::toString).collect(Collectors.joining(","))
                + ")";
    }
}
