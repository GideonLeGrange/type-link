package me.legrange.typelink.lambda.lookup;

import me.legrange.typelink.Selects;
import me.legrange.typelink.lambda.DecoderException;
import me.legrange.typelink.lambda.structure.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public final class Static {

    public static Optional<VirtualMethod> lookupStatic(Method method) throws DecoderException {
        return lookupStatic(method.getDeclaringClass(), method.getName());
    }

    private static Optional<VirtualMethod> lookupStatic(Class<?> targetClass, String methodName) throws DecoderException {
        if (Collection.class.isAssignableFrom(targetClass)) {
            return collection(methodName);
        }
        if (Number.class.isAssignableFrom(targetClass)) {
            return number(methodName);
        }
        if (Boolean.class.isAssignableFrom(targetClass)) {
            return bool(methodName);
        }
        if (Selects.class.isAssignableFrom(targetClass)) {
            return selects(methodName);
        }
        return Optional.empty();
    }

    private static Optional<VirtualMethod> collection(String methodName) throws DecoderException {
        return ofNullable(switch (methodName) {
            case "of" -> new VirtualMethod(ListValue::new);
            default -> null;
        });
    }

    private static Optional<VirtualMethod> number(String methodName) {
        return ofNullable(switch (methodName) {
            case "valueOf" -> new VirtualMethod(List::getFirst);
            default -> null;
        });
    }

    private static Optional<VirtualMethod> bool(String methodName) {
        return ofNullable(switch (methodName) {
            case "valueOf" -> new VirtualMethod(List::getFirst);
            default -> null;
        });
    }

    private static Optional<VirtualMethod> selects(String methodName) {
        return ofNullable(switch (methodName) {
            case "sum" -> new VirtualMethod(list -> new Sum(list.getFirst()));
            case "count" -> new VirtualMethod(list -> new Count(list.isEmpty() ? null : list.getFirst()));
            case "avg" -> new VirtualMethod(list -> new Avg(list.getFirst()));
            case "min" -> new VirtualMethod(list -> new Min(list.getFirst()));
            case "max" -> new VirtualMethod(list -> new Max(list.getFirst()));
            default -> null;
        });
    }

}
