package me.legrange.typelink.lambda.structure;

import java.lang.reflect.Method;

/**
 * A reference to a method, as in {@code Reminder::getDue}.
 * <p>
 * A method reference names a method rather than containing code, so what it means depends on the method. Some are
 * just a read of a column, which a mapper can recognise from the {@link Method} alone. Others are small methods whose
 * body is worth expanding. Both are available: the method, and the decoded body, which is only worked out if asked
 * for.
 *
 * @param method the method referred to
 * @param body   what the method's body decodes to
 */
public record MethodReference(Method method, Body body) implements Value {

    /** A reference with no body to offer, such as one the lambda only passes along. */
    public MethodReference(Method method) {
        this(method, Body.NONE);
    }

    /** The decoded body of a method, produced on first use. */
    @FunctionalInterface
    public interface Body {

        /** There is no body to decode. */
        Body NONE = () -> {
            throw new IllegalStateException("No body was recorded for this method reference");
        };

        Value get();
    }

    /** Whether {@link #body()} can be asked for. */
    public boolean hasBody() {
        return body != Body.NONE;
    }

    @Override
    public Class<?> type() {
        return method.getReturnType();
    }

    // the body is a way to get at the code, not part of what the reference is
    @Override
    public boolean equals(Object other) {
        return other instanceof MethodReference ref && ref.method.equals(method);
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public String toString() {
        return method.getDeclaringClass().getSimpleName() + "::" + method.getName();
    }
}
