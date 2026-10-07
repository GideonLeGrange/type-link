package me.legrange.typelink.sql.parser;

import me.legrange.typelink.TableMapper;
import me.legrange.typelink.lambda.structure.Argument;
import me.legrange.typelink.lambda.structure.Lambda;
import me.legrange.typelink.lambda.structure.MethodCall;
import me.legrange.typelink.lambda.structure.MethodReference;
import me.legrange.typelink.lambda.structure.Value;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * What a lambda means once the mapping is known. Most lambdas are their body. A method reference is a method: if the
 * mapper says it reads a column it means that column, as {@code row -> row.method()} would, and otherwise it means
 * the method's body, as it always did.
 */
public final class LambdaValues {

    private LambdaValues() {
    }

    /**
     * @param types the tables of the scope the lambda belongs to, so a column is read from the right one; empty when
     *              the caller has none, which falls back to the class that declares the method
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Value of(TableMapper mapper, List<Class<?>> types, Lambda lambda) {
        if (!(lambda.value() instanceof MethodReference ref)) {
            return lambda.value();
        }
        var method = ref.method();
        var row = types.isEmpty() ? method.getDeclaringClass() : types.getFirst();
        if (!Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0 && mapper.isColumn(row, method)) {
            return new MethodCall(new Argument(0, 0, 0, 0, "row", row), method, List.of());
        }
        return ref.hasBody() ? ref.body().get() : ref;
    }
}
