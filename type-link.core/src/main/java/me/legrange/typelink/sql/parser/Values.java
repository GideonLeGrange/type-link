package me.legrange.typelink.sql.parser;

import me.legrange.typelink.lambda.structure.*;
import me.legrange.typelink.sql.structure.SqlConstant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.String.format;

final class Values {

    private Values() {
    }

    /**
     * Resolve the value an {@link Argument} captured.
     *
     * <p>Keyed on the argument's position in the parameter list rather than its JVM slot: a
     * {@code long} or {@code double} occupies two slots, so the two numbers diverge as soon as a
     * wide capture appears and slot-based lookup reads the wrong entry or runs off the end.
     */
    static Object capturedArgument(Context context, Argument arg) {
        var args = context.args();
        if (arg.index() < 0 || arg.index() >= args.size()) {
            throw new QueryParseException(format(
                    "Cannot resolve captured value for argument '%s': position %d is outside the %d captured argument(s). BUG!",
                    arg.name(), arg.index(), args.size()));
        }
        return args.get(arg.index());
    }

    static SqlConstant fieldValue(StaticFieldReference value) {
        try {
            var field = value.field();
            if (!field.canAccess(null)) {
                field.setAccessible(true);
            }
            return new SqlConstant(field.get(null));
        } catch (IllegalAccessException e) {
            throw new QueryParseException(e.getMessage(), e);
        }
    }

    static Object fieldValue(Context context, InstanceFieldReference f) {
        var field = f.field();
        var target = f.target();
        if (!(target instanceof Argument arg)) {
            throw new QueryParseException(format("Unexpected %s field target. BUG!", field.getName()));
        }
        var object = capturedArgument(context, arg);
        if (!field.canAccess(object)) {
            field.setAccessible(true);
        }
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new QueryParseException(format("Error accessing field %s (%s)",
                    f.field().getName(), e.getMessage()), e);
        }

    }

    static Object callMethod(Context context, MethodCall methodCall) {
        var target = methodCall.target();
        return switch (target) {
            case Argument arg ->
                    callMethod(capturedArgument(context, arg), methodCall.method(), methodParams(context, methodCall));
            case MethodCall mc ->
                    callMethod(callMethod(context, mc), methodCall.method(), methodParams(context, methodCall));
            case InstanceFieldReference fi ->
                    callMethod(fieldValue(context, fi), methodCall.method(), methodParams(context, methodCall));
            case StaticMethodCall sm ->
                    callMethod(callStaticMethod(sm), methodCall.method(), methodParams(context, methodCall));
            case StaticFieldReference sf ->
                    callMethod(valueForStaticField(sf), methodCall.method(), methodParams(context, methodCall));
            default ->
                    throw new QueryParseException(format("Unexpected %s method target. BUG!", target.getClass().getSimpleName()));
        };
    }

    static Object callStaticMethod(StaticMethodCall sm) {
        try {
            if (!sm.method().canAccess(null)) {
                sm.method().setAccessible(true);
            }
            var args = sm.parameters().stream().map(Values::value).toList().reversed().toArray();
            return sm.method().invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            throw new QueryParseException(e.getMessage(), e);
        }
    }

    static int intValue(Value value) {
        if (!(value instanceof Constant<?>(Integer i))) {
            throw new QueryParseException(format("Cannot convert %s to int. BUG!", value.getClass().getSimpleName()));
        }
        return i;
    }

    private static Object callMethod(Object target, Method method, Object[] args) {
        if (!method.canAccess(target)) {
            method.setAccessible(true);
        }
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new QueryParseException(format("Error calling method %s on %s (%s)",
                    method.getName(), target.getClass().getSimpleName(), e.getMessage()), e);
        }
    }

    private static Object[] methodParams(Context context, MethodCall methodCall) {
        return methodCall.parameters().stream().map(v -> methodParam(context, v)).toArray();
    }

    private static Object methodParam(Context context, Value value) {
        return switch (value) {
            case Argument argument -> capturedArgument(context, argument);
            case Constant<?> constant -> constant.value();
            default ->
                    throw new QueryParseException(format("Unexpected %s method parameter. BUG!", value.getClass().getSimpleName()));
        };
    }

    private static Object valueForStaticField(StaticFieldReference sf) {
        try {
            if (!sf.field().canAccess(null)) {
                sf.field().setAccessible(true);
            }
            return sf.field().get(null);
        } catch (IllegalAccessException e) {
            throw new QueryParseException(e.getMessage(), e);
        }
    }

    private static Object value(Value value) {
        if (value instanceof Constant<?>(Object val)) {
            return val;
        }
        throw new QueryParseException(format("Unexpected %s value. BUG!", value.getClass().getSimpleName()));
    }

}
