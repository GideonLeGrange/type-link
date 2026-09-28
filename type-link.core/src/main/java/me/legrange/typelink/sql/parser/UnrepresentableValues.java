package me.legrange.typelink.sql.parser;

import me.legrange.typelink.lambda.structure.Expression;
import me.legrange.typelink.lambda.structure.ListValue;
import me.legrange.typelink.lambda.structure.MethodReference;
import me.legrange.typelink.lambda.structure.StaticMethodCall;
import me.legrange.typelink.lambda.structure.Value;

import java.util.Optional;

/**
 * The {@link Value} shapes {@link ColumnResolver#column} refuses unconditionally - not because of
 * anything about the query they appear in, but because the shape itself has no general SQL
 * translation, whatever table or column it would otherwise touch. Extracted here, rather than left
 * inline in {@code column()}, so a caller with no {@link Context} to offer - such as the
 * compile-time {@code validate-queries} plugin, checking a lambda in isolation - can still ask
 * whether a decoded value could ever be selected, without needing to build one.
 */
public final class UnrepresentableValues {

    private UnrepresentableValues() {
    }

    /**
     * Empty if {@code value} could be a SELECT column in some context; otherwise, a plain-English
     * reason it never could be, regardless of table or column mapping.
     */
    public static Optional<String> reasonNotSelectable(Value value) {
        return switch (value) {
            case StaticMethodCall smc -> Optional.of("a static method call (" + smc.method().getName() + ")");
            case MethodReference mr -> Optional.of("a method reference (" + mr.method().getName() + ")");
            case ListValue _ -> Optional.of("a list (List.of(...))");
            case Expression _ -> Optional.of("a boolean expression");
            default -> Optional.empty();
        };
    }
}
