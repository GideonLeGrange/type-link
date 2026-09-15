package me.legrange.typelink.lambda.structure;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public record MethodCall(Value target, Method method, List<Value> parameters) implements Call {

    public String toString() {
        return target + "." + method.getName() + "(" +
                parameters.stream().map(Object::toString).collect(Collectors.joining(","))
                + ")";
    }

    @Override
    public Class<?> type() {
        return method.getReturnType();
    }
}
