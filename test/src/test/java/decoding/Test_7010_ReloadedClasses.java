package decoding;

import me.legrange.typelink.lambda.parser.LambdaParser;
import org.junit.jupiter.api.Test;

import javax.tools.ToolProvider;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A lambda is decoded from the class as it is now, not as a class of that name once was.
 *
 * <p><b>The bug this guards.</b> What the decoder read of a class was kept under the class's name. An application that
 * reloads its classes after an edit (Quarkus in dev mode) gets a new class with the same name, and the decoder went on using
 * what it had read of the old one: the query still said what it used to say, or the lambda's method was not found.
 */
class Test_7010_ReloadedClasses {

    private static final String PACKAGE = "reloaded";

    private static String source(int limit) {
        return "package " + PACKAGE + ";\n"
                + "import me.legrange.typelink.QueryPredicate1;\n"
                + "import rec.Invoice;\n"
                + "public class Probe {\n"
                + "    public static QueryPredicate1<Invoice> predicate() { return i -> i.id() > " + limit + "; }\n"
                + "}\n";
    }

    /** The class file of reloaded.Probe, compiled from the source */
    private static byte[] compile(String source) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var out = new ByteArrayOutputStream();
        var unit = new SimpleJavaFileObject(URI.create("string:///" + PACKAGE + "/Probe.java"), javax.tools.JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        var manager = compiler.getStandardFileManager(null, null, null);
        var forwarding = new javax.tools.ForwardingJavaFileManager<>(manager) {
            @Override
            public javax.tools.JavaFileObject getJavaFileForOutput(Location location, String className, javax.tools.JavaFileObject.Kind kind,
                                                                   javax.tools.FileObject sibling) {
                return new SimpleJavaFileObject(URI.create("bytes:///" + className), kind) {
                    @Override
                    public java.io.OutputStream openOutputStream() {
                        return out;
                    }
                };
            }
        };
        var classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        var ok = compiler.getTask(null, forwarding, null, List.of("-classpath", classpath, "-g"), null, List.of(unit)).call();
        if (!ok) {
            throw new IllegalStateException("could not compile the probe");
        }
        return out.toByteArray();
    }

    /** A loader that has the probe class itself, and its class file as a resource, and leaves everything else to its parent */
    private static final class ProbeLoader extends ClassLoader {
        private final byte[] bytes;

        ProbeLoader(byte[] bytes) {
            super(Test_7010_ReloadedClasses.class.getClassLoader());
            this.bytes = bytes;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.equals(PACKAGE + ".Probe")) {
                synchronized (getClassLoadingLock(name)) {
                    var loaded = findLoadedClass(name);
                    return loaded != null ? loaded : defineClass(name, bytes, 0, bytes.length);
                }
            }
            return super.loadClass(name, resolve);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return name.equals(PACKAGE + "/Probe.class") ? new ByteArrayInputStream(bytes) : super.getResourceAsStream(name);
        }
    }

    private static Object decode(byte[] classFile) throws Exception {
        var loader = new ProbeLoader(classFile);
        var thread = Thread.currentThread();
        var before = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        try {
            var probe = loader.loadClass(PACKAGE + ".Probe");
            var predicate = (Serializable) probe.getMethod("predicate").invoke(null);
            return LambdaParser.parse(predicate).value();
        } finally {
            thread.setContextClassLoader(before);
        }
    }

    @Test
    void aClassThatIsReloadedWithAnotherBodyIsDecodedFromItsNewBody() throws Exception {
        var first = compile(source(1));
        var second = compile(source(2));

        var before = decode(first);
        var after = decode(second);

        assertNotEquals(before, after, "the second version of the class has another limit, so it decodes to another expression");
        assertEquals(before, decode(first), "the same class file decodes to the same expression");
    }
}
