package me.legrange.typelink.lambda.lookup;


import me.legrange.typelink.lambda.DecoderException;
import me.legrange.typelink.lambda.structure.*;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public final class Virtual {

    public static Optional<VirtualMethod> lookupVirtual(Method method) throws DecoderException {
        return lookupVirtual(method.getDeclaringClass(), method.getName(), method.getParameterCount());
    }

    private static Optional<VirtualMethod> lookupVirtual(Class<?> targetClass, String methodName, int paramCount) throws DecoderException {
        if (Collection.class.isAssignableFrom(targetClass)) {
            return collection(methodName, paramCount);
        }
        if (Number.class.isAssignableFrom(targetClass)) {
            return number(methodName, paramCount);
        }
        if (String.class.isAssignableFrom(targetClass)) {
            return string(methodName, paramCount);
        }
        if (LocalDate.class.isAssignableFrom(targetClass)) {
            return localDate(methodName, paramCount);
        }
        if (LocalDateTime.class.isAssignableFrom(targetClass)) {
            return localDate(methodName, paramCount);
        }
        return object(methodName, paramCount);
    }

    private static Optional<VirtualMethod> string(String methodName, int paramCount) throws DecoderException {
        return ofNullable(switch (methodName) {
            case "startsWith" -> paramCount == 1
                    ? new VirtualMethod(list -> new StartsWith(list.getFirst(), list.getLast()))
                    : null;
            case "endsWith" -> paramCount == 1
                    ? new VirtualMethod(list -> new EndsWith(list.getFirst(), list.getLast()))
                    : null;
            case "equals" -> paramCount == 1
                    ? new VirtualMethod(list -> new Eq(list.getFirst(), list.getLast()))
                    : null;
            case "contains" -> paramCount == 1
                    ? new VirtualMethod(list -> new Contains(list.getFirst(), list.getLast()))
                    : null;
            default -> null;
        });
    }

    private static Optional<VirtualMethod> localDate(String methodName, int paramCount) throws DecoderException {
        return ofNullable(switch (methodName) {
            case "isAfter" -> switch (paramCount) {
                case 1 -> new VirtualMethod(list -> new IsAfter(list.getFirst(), list.getLast()));
                default -> null;
            };
            case "isBefore" -> switch (paramCount) {
                case 1 -> new VirtualMethod(list -> new IsBefore(list.getFirst(), list.getLast()));
                default -> null;
            };
            case "equals" -> switch (paramCount) {
                case 1 -> new VirtualMethod(list -> new Eq(list.getFirst(), list.getLast()));
                default -> null;
            };
            default -> null;
        });
    }

    private static Optional<VirtualMethod> collection(String methodName, int paramCount) throws DecoderException {
        return ofNullable(switch (methodName) {
            case "contains" -> switch (paramCount) {
                case 1 -> new VirtualMethod(list -> new InSet(list.getLast(), list.getFirst()));
                default -> null;
            };
            default -> null;
        });
    }

    private static Optional<VirtualMethod> object(String methodName, int paramCount) {
        return ofNullable(switch (methodName) {
            case "equals" -> switch (paramCount) {
                case 1 -> new VirtualMethod(list ->
                        new Eq(list.getFirst(), list.getLast()));
                default -> null;
            };
            default -> null;
        });
    }

    private static Optional<VirtualMethod> number(String methodName, int paramCount) {
        return ofNullable(switch (methodName) {
            case "equals" -> switch (paramCount) {
                case 1 -> new VirtualMethod(list -> new Eq(list.getFirst(), list.getLast()));
                default -> null;
            };
            case "longValue", "intValue", "byteValue", "shortValue", "floatValue", "doubleValue" ->
                    switch (paramCount) {
                        case 0 -> new VirtualMethod(List::getFirst);
                        default -> null;
                    };
            default -> null;
        });
    }

}
