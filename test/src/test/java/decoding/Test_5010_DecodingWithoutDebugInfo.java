package decoding;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import rec.Invoice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.instruction.LocalVariable;
import java.lang.classfile.instruction.LocalVariableType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test: lambda decoding must not depend on the {@code LocalVariableTable} attribute.
 *
 * <p><b>The bug this guards.</b> {@code BytecodeParser} used to populate its slot table only from
 * {@code LocalVariable} code elements. Those are synthesised by the class file reader from the
 * {@code LocalVariableTable} attribute, which javac emits only under {@code -g:vars}. Against class
 * files compiled without it every argument load pushed {@code null}, and decoding died much later
 * with a {@code NullPointerException} on {@code Value.type()} inside {@code ColumnResolver} -
 * nothing in the message pointed at a missing class file attribute. The parser now seeds slots from
 * the method descriptor, which is always present.
 *
 * <p><b>Why the test looks like this.</b> Maven compiles this module with debug information on, so
 * a plain test cannot reach the no-debug path. Rather than requiring a second build, the test takes
 * the fixture's own compiled bytes, strips every {@code LocalVariable} element out of them, and
 * serves the result through a context class loader - which is where the decoder looks for class
 * bytes first. The alternative is a CI profile running the whole suite with
 * {@code -Dmaven.compiler.debug=false}, which is a good idea independently but does not belong to
 * any single test.
 *
 * <p><b>The test validates its own premise</b> before asserting anything: it checks the fixture
 * really had local variable information and that stripping really removed it. Without that, a
 * change to the transform would leave this passing while testing nothing.
 */
class Test_5010_DecodingWithoutDebugInfo {

    private static final String FIXTURE_RESOURCE = "decoding/StrippedFixture.class";

    @Test
    void decodesPredicatesWithNoLocalVariableTable() throws Exception {
        var original = readResource(FIXTURE_RESOURCE);
        Assumptions.assumeTrue(countLocalVariables(original) > 0,
                "fixture was compiled without debug information, so there is nothing to strip - "
                        + "the whole suite is already exercising the no-debug path");

        var stripped = stripLocalVariables(original);
        assertEquals(0, countLocalVariables(stripped),
                "stripping did not remove the local variable information, so this test would prove nothing");
        assertTrue(stripped.length > 0);

        withClassBytes(FIXTURE_RESOURCE, stripped, () -> {
            // A single narrow capture: nothing here depends on capture positions, so a failure
            // points at the descriptor seeding and nowhere else.
            var db = new CapturingDatabase();
            db.from(Invoice.class).where(StrippedFixture.ownedBy(7L)).list();
            assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.clientId = ?", db.sql());
            assertEquals(List.of(7L), db.params());

            // A method reference decodes to an instance method, where slot 0 is `this`. That slot
            // has to be seeded too - an early version of the fix missed it, and only method
            // references caught it.
            var reference = new CapturingDatabase();
            reference.from(Invoice.class).where(StrippedFixture.isPaid()).list();
            assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.paid = ?", reference.sql());
        });
    }

    /**
     * Deliberately depends on both fixes at once: descriptor seeding to decode at all without the
     * attribute, and position-based capture lookup to get a wide capture's neighbours right.
     *
     * <p>Kept separate from the case above so that a failure here, with that one passing, points at
     * capture positions rather than at debug information. See
     * {@link Test_5000_CapturedArgumentPositions} for the positions in isolation.
     */
    @Test
    void capturePositionsSurviveStripping() throws Exception {
        var original = readResource(FIXTURE_RESOURCE);
        Assumptions.assumeTrue(countLocalVariables(original) > 0,
                "fixture was compiled without debug information, so there is nothing to strip");

        var stripped = stripLocalVariables(original);
        assertEquals(0, countLocalVariables(stripped), "stripping did not remove the local variable information");

        withClassBytes(FIXTURE_RESOURCE, stripped, () -> {
            var db = new CapturingDatabase();
            db.from(Invoice.class).where(StrippedFixture.ownedByAbove(7L, 30)).list();
            assertEquals(List.of(7L, 30), db.params());
        });
    }

    /** Run {@code action} with a context class loader that serves {@code bytes} for {@code resource}. */
    private static void withClassBytes(String resource, byte[] bytes, ThrowingRunnable action) throws Exception {
        var thread = Thread.currentThread();
        var previous = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(new OverridingLoader(previous, resource, bytes));
            action.run();
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

    private static byte[] stripLocalVariables(byte[] original) {
        var classFile = ClassFile.of();
        return classFile.transformClass(classFile.parse(original),
                ClassTransform.transformingMethodBodies((builder, element) -> {
                    if (!(element instanceof LocalVariable) && !(element instanceof LocalVariableType)) {
                        builder.with(element);
                    }
                }));
    }

    private static long countLocalVariables(byte[] bytes) {
        return ClassFile.of().parse(bytes).methods().stream()
                .flatMap(method -> method.code().stream())
                .flatMap(code -> code.elementList().stream())
                .filter(element -> element instanceof LocalVariable)
                .count();
    }

    private static byte[] readResource(String resource) throws IOException {
        try (var stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Cannot find " + resource + " on the classpath");
            }
            return stream.readAllBytes();
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Serves replacement bytes for one resource and delegates everything else to its parent. */
    private static final class OverridingLoader extends ClassLoader {

        private final String resource;
        private final byte[] bytes;

        OverridingLoader(ClassLoader parent, String resource, byte[] bytes) {
            super(parent);
            this.resource = resource;
            this.bytes = bytes;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return resource.equals(name) ? new ByteArrayInputStream(bytes) : super.getResourceAsStream(name);
        }
    }
}
