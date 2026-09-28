package decoding;

import me.legrange.typelink.SelectFunction1;
import me.legrange.typelink.lambda.DecoderException;
import me.legrange.typelink.lambda.parser.BytecodeParser;
import org.junit.jupiter.api.Test;
import rec.Invoice;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeModel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Some Java constructs have no SQL translation and never will - modulo, bitwise operators, {@code
 * switch}, a varargs call, a ternary yielding an arbitrary (non 0/1) value. Refusing them is correct.
 * The problem is how: every one of these goes through {@link BytecodeParser#bug} - the same path used
 * for an actual internal defect - so the user is told "BUG!" for a deliberate, permanent limitation,
 * and the message quotes raw JVM terms ({@code IREM}, {@code LOOKUPSWITCH}, {@code ANEWARRAY}, "jump")
 * that mean nothing to someone who only ever wrote a lambda.
 *
 * <p><b>These tests currently fail</b>: today's messages say "BUG!" and leak exactly the internal
 * terms asserted against below. They are written against the fix - clear, non-BUG wording that names
 * the construct in terms a query author recognises - not against today's wording.
 */
class Test_6060_CrypticUnsupportedConstructMessages {

    private record Numbers(int count) {
    }

    @Test
    void moduloIsRefusedWithAClearMessage() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Numbers, ?> unused = n -> n.count() % 3 == 0;

        var message = decodeAndCaptureMessage("moduloIsRefusedWithAClearMessage");

        assertNotCryptic(message, "IREM", "opcode");
        assertTrue(message.contains("%"), "expected the message to name modulo (%), got: " + message);
    }

    @Test
    void bitwiseAndIsRefusedWithAClearMessage() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Numbers, ?> unused = n -> (n.count() & 1) == 0;

        var message = decodeAndCaptureMessage("bitwiseAndIsRefusedWithAClearMessage");

        assertNotCryptic(message, "IAND", "opcode");
        assertTrue(message.contains("&"), "expected the message to name the bitwise operator (&), got: " + message);
    }

    @Test
    void bitwiseShiftIsRefusedWithAClearMessage() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Numbers, ?> unused = n -> (n.count() << 1) == 0;

        var message = decodeAndCaptureMessage("bitwiseShiftIsRefusedWithAClearMessage");

        assertNotCryptic(message, "ISHL", "opcode");
        assertTrue(message.contains("<<"), "expected the message to name the shift operator (<<), got: " + message);
    }

    @Test
    void switchIsRefusedWithAClearMessage() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Numbers, ?> unused = n -> switch (n.count()) {
            case 1 -> "one";
            default -> "other";
        };

        var message = decodeAndCaptureMessage("switchIsRefusedWithAClearMessage");

        assertNotCryptic(message, "LookupSwitch", "LOOKUPSWITCH", "TableSwitch", "TABLESWITCH",
                "value element");
        assertTrue(message.toLowerCase().contains("switch"),
                "expected the message to name switch, got: " + message);
    }

    @Test
    void varargsCallIsRefusedWithAClearMessage() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Invoice, ?> unused = i -> String.format("%d", i.clientId());

        var message = decodeAndCaptureMessage("varargsCallIsRefusedWithAClearMessage");

        assertNotCryptic(message, "NewRefArray", "ANEWARRAY", "value element");
        assertTrue(message.toLowerCase().contains("vararg"),
                "expected the message to name varargs, got: " + message);
    }

    @Test
    void ternaryReturningNonBooleanValuesIsRefusedWithAClearMessage() throws IOException {
        @SuppressWarnings("unused")
        SelectFunction1<Invoice, ?> unused = i -> i.paid() ? 100 : 200;

        var message = decodeAndCaptureMessage("ternaryReturningNonBooleanValuesIsRefusedWithAClearMessage");

        assertNotCryptic(message, "jump");
        assertTrue(message.toLowerCase().contains("ternary") || message.toLowerCase().contains("conditional"),
                "expected the message to name the ternary/conditional expression, got: " + message);
    }

    private static void assertNotCryptic(String message, String... internalTerms) {
        assertFalse(message.contains("BUG!"),
                "a deliberate, permanent refusal is not a bug, but got: " + message);
        for (var term : internalTerms) {
            assertFalse(message.contains(term),
                    "expected no leak of the internal term '" + term + "', got: " + message);
        }
    }

    private static String decodeAndCaptureMessage(String testMethodName) throws IOException {
        var model = lambdaCodeModel(testMethodName);
        var exception = assertThrows(DecoderException.class, () -> BytecodeParser.parseBytecode(model, 0));
        return exception.getMessage();
    }

    /**
     * Finds the {@code CodeModel} for the one-parameter lambda declared in the named test method, by
     * parsing this test class's own compiled bytecode - the same source {@code ValidateQueries} reads
     * its {@code CodeModel}s from.
     */
    private static CodeModel lambdaCodeModel(String testMethodName) throws IOException {
        byte[] bytes;
        try (var in = Test_6060_CrypticUnsupportedConstructMessages.class
                .getResourceAsStream("Test_6060_CrypticUnsupportedConstructMessages.class")) {
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
