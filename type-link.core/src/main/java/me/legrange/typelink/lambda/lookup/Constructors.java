package me.legrange.typelink.lambda.lookup;

import me.legrange.typelink.Selector;
import me.legrange.typelink.lambda.structure.Count;

import java.lang.reflect.Constructor;
import java.util.Optional;

public final class Constructors {

    private Constructors() {
    }

    public static Optional<VirtualMethod> lookupConstructor(Constructor<?> constructor) {
        return lookupConstructor(constructor.getDeclaringClass(), constructor.getParameterCount());
    }

    private static Optional<VirtualMethod> lookupConstructor(Class<?> type, int paramCount) {
        if (Selector.class.isAssignableFrom(type)) {
            return selector(type, paramCount);
        }
        return Optional.empty();
    }

    private static Optional<VirtualMethod> selector(Class<?> type, int paramCount) {
        if (Selector.class.isAssignableFrom(type)) {
            if (paramCount == 1) {
                return Optional.of(new VirtualMethod(list -> new Count(list.getFirst())));
            }
            return Optional.of(new VirtualMethod(_ -> new Count(null)));
        }
        return Optional.empty();
    }
}
