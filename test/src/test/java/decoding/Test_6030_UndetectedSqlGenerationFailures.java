package decoding;

import me.legrange.typelink.SelectFunction1;
import me.legrange.typelink.lambda.parser.BytecodeParser;
import org.junit.jupiter.api.Test;
import rec.Invoice;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeModel;
import java.util.List;
import java.util.function.Function;

import static me.legrange.typelink.sql.parser.UnrepresentableValues.reasonNotSelectable;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Some lambdas decode without error but produce a {@link me.legrange.typelink.lambda.structure.Value}
 * shape that has no general SQL translation as a selected value - not because of anything about the
 * query it appears in, but because the shape itself (a raw list, a bare boolean expression, a static
 * method call, a method reference) can never be a column, whatever table or mapping it would otherwise
 * touch.
 *
 * <p><b>The gap this targets.</b> {@code ValidateQueries} calls {@link BytecodeParser#parseBytecode}
 * directly and never builds a {@code Context} or reaches {@code ColumnResolver} - that class is only
 * ever reached through {@code Link}/{@code LambdaParser}, which the plugin does not use. Since none of
 * these four shapes were ever valid regardless of context (no table/column mapping changes the
 * answer), {@link me.legrange.typelink.sql.parser.UnrepresentableValues} extracts that
 * context-independent half of {@code ColumnResolver}'s refusal into a small, pure classifier that
 * takes a decoded {@code Value} and nothing else - callable by the plugin without it needing to
 * reconstruct a query.
 *
 * <p>{@code ValidateQueries} composes it with {@link BytecodeParser#resolve}, but only for a
 * value-producing lambda: a predicate (boolean return) is consumed by {@code ClauseBuilder} instead,
 * which fully supports a bare expression or a list, so the same shapes are not refused there. Telling
 * the two apart needs to know which kind of lambda this call site declared - knowledge only the plugin
 * has, not something {@code BytecodeParser} itself should need to care about.
 *
 * <p>Each test below builds a lambda whose only job is to produce one of these shapes, locates the
 * synthetic method javac compiled it into (the same way {@code ValidateQueries} locates a lambda body
 * from an {@code invokedynamic} call site), and runs the same two calls the plugin now makes.
 */
class Test_6030_UndetectedSqlGenerationFailures {

    @Test
    void staticMethodCallIsCaught() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Invoice, ?> unused = i -> Math.abs(i.amount());

        assertCaughtByThePlugin("staticMethodCallIsCaught");
    }

    @Test
    void booleanExpressionSelectedAsAValueIsCaught() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Invoice, ?> unused = i -> i.amount() > 100;

        assertCaughtByThePlugin("booleanExpressionSelectedAsAValueIsCaught");
    }

    @Test
    void listValueSelectedAsAValueIsCaught() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Invoice, ?> unused = i -> List.of(i.clientId(), i.amount());

        assertCaughtByThePlugin("listValueSelectedAsAValueIsCaught");
    }

    @Test
    void methodReferenceSelectedAsAValueIsCaught() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Invoice, ?> unused = i -> (Function<Invoice, String>) Invoice::description;

        assertCaughtByThePlugin("methodReferenceSelectedAsAValueIsCaught");
    }

    /**
     * Runs exactly what {@code ValidateQueries} runs for a value-producing (non-boolean) call site:
     * decode, then resolve to the value the lambda ultimately stands for, then classify it.
     */
    private static void assertCaughtByThePlugin(String testMethodName) throws IOException {
        var model = lambdaCodeModel(testMethodName);
        var value = BytecodeParser.resolve(BytecodeParser.parseBytecode(model, 0));

        assertTrue(reasonNotSelectable(value).isPresent(),
                "expected " + value.getClass().getSimpleName() + " to be a shape ValidateQueries "
                        + "flags as unrepresentable, but it was accepted");
    }

    /**
     * Finds the {@code CodeModel} for the one-parameter lambda declared in the named test method, by
     * parsing this test class's own compiled bytecode - the same source {@code ValidateQueries} reads
     * its {@code CodeModel}s from, as opposed to {@link java.lang.invoke.SerializedLambda}, which the
     * plugin has no lambda instance to obtain.
     */
    private static CodeModel lambdaCodeModel(String testMethodName) throws IOException {
        byte[] bytes;
        try (var in = Test_6030_UndetectedSqlGenerationFailures.class
                .getResourceAsStream("Test_6030_UndetectedSqlGenerationFailures.class")) {
            bytes = in.readAllBytes();
        }
        var classModel = ClassFile.of().parse(bytes);
        var prefix = "lambda$" + testMethodName + "$";
        var method = classModel.methods().stream()
                .filter(m -> m.methodName().stringValue().startsWith(prefix))
                .filter(m -> m.methodTypeSymbol().parameterCount() == 1)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no one-parameter lambda body found for " + testMethodName));
        return method.code().orElseThrow(() -> new AssertionError("lambda body has no code"));
    }
}
