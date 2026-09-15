package me.legrange.typelink.lambda.structure;

import java.lang.reflect.Method;

public record MethodReference(Method method) implements Value {

    @Override
    public Class<?> type() {
        return method.getReturnType();
    }
}
