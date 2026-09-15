package me.legrange.typelink.lambda.structure;

import java.lang.reflect.Method;
import java.util.List;

public record StaticMethodCall(Method method, List<Value> parameters) implements Call {
    @Override
    public Class<?> type() {
        return method.getReturnType();
    }
}
