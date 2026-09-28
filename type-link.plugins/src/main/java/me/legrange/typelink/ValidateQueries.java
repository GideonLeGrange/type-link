package me.legrange.typelink;

import me.legrange.typelink.lambda.BytecodeParseException;
import me.legrange.typelink.lambda.ClassDecodingException;
import me.legrange.typelink.lambda.DecoderException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.classfile.*;
import java.lang.classfile.instruction.InvokeDynamicInstruction;
import java.lang.classfile.instruction.LineNumber;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static me.legrange.typelink.lambda.parser.BytecodeParser.parseBytecode;
import static me.legrange.typelink.lambda.parser.BytecodeParser.resolve;
import static me.legrange.typelink.sql.parser.UnrepresentableValues.reasonNotSelectable;

/**
 * Maven Mojo for validating typelink queries.
 * Loads all compiled classes from the project so they can be evaluated.
 */
// FIXME this plugin contains copied code from ClassUtil. ClassUtil probably needs to be
// reworked to accept a class loader, or at least have some methods exposed.
@Mojo(name = "validate-queries", defaultPhase = LifecyclePhase.VALIDATE,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class ValidateQueries extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private String outputDirectory;
    private ClassLoader loader;
    private int lineNumber;
    private final Set<Result> results = new HashSet<>();

    public void execute() throws MojoExecutionException {
        var packaging = project.getPackaging();
        if ("pom".equals(packaging) || "maven-archetype".equals(packaging)) {
            // Skip processing
            return;
        }
        getLog().info("Loading compiled classes from project...");
        try {
            loader = createProjectClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            var loadedClasses = loadAllClasses(new File(outputDirectory), loader, "");

            getLog().info(format("Processing  %d classes", loadedClasses.size()));
            for (var clazz : loadedClasses) {
                var fileName = topLevelClassOf(clazz).getCanonicalName().replace(".", "/") + ".java";
                results.addAll(validate(clazz, fileName));
            }
            if (!results.isEmpty()) {
                var errors = results.stream()
                        .filter(result -> result instanceof Error)
                        .map(result -> (Error) result).toList();
                for (var error : errors.stream()
                        .sorted(Comparator.comparing(Error::fileName).thenComparing(Error::lineNumber)).toList()) {
                    getLog().error(format("Unsupported lambda code at line %d in %s: %s",
                            error.lineNumber(), error.fileName(), error.error()));
                }
                if (!errors.isEmpty()) {
                    getLog().error("");
                    getLog().error("The most likely cause of these errors is one of the following:");
                    getLog().error("");
                    getLog().error("""
                        • A query lambda, while looking as if it can map to valid SQL, generates an unexpected Java bytecode.
                        If you feel this is the case, report it""");
                    getLog().error("""
                        • A query lambda cannot be represented in SQL. Review the lambda to ensure it is doing what
                         you want it to do, and if so, consider rewriting it in a way that is more likely to be supported.""");
                    getLog().error("");
                    throw new MojoFailureException(format("There %s %d lambda%s with errors. Queries will fail at runtime",
                            errors.size() == 1 ? "is" : "are",
                            errors.size(),
                            errors.size() == 1 ? "" : "s"));
                }
            }
            getLog().info(format("Processed %d classes, evaluated %d lambdas, %d OK and %d errors",
                    loadedClasses.size(), results.size(), results.stream().filter(result -> result instanceof Ok).count(),
                    results.stream().filter(result -> (result instanceof Error)).count()
            ));

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to load project classes", e);
        }
    }

    /**
     * Creates a URLClassLoader that includes the project's output directory and its compile
     * classpath, so that supertypes and interfaces declared in dependencies (e.g. marker
     * interfaces) can be resolved while loading the project's own classes.
     */
    private ClassLoader createProjectClassLoader() throws Exception {
        var urls = new ArrayList<URL>();
        urls.add(makeUrl(outputDirectory));
        for (var element : project.getCompileClasspathElements()) {
            urls.add(new File((String) element).toURI().toURL());
        }
        return new URLClassLoader(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
    }

    /**
     * A nested class lives in its enclosing class's .java file, not one of its own.
     */
    private Class<?> topLevelClassOf(Class<?> type) {
        var enclosing = type.getEnclosingClass();
        return enclosing == null ? type : topLevelClassOf(enclosing);
    }

    private URL makeUrl(String dir) throws MalformedURLException {
        var file = new File(dir);
        if (!file.exists()) {
            throw new IllegalStateException("Output directory does not exist: " + dir);
        }
        return file.toURI().toURL();
    }

    private List<Result> validate(Class<?> type, String fileName) {
        // Binary name, not canonical name: a nested class's .class file is Outer$Inner.class,
        // and getCanonicalName() renders that boundary as a dot instead of a dollar sign.
        var model = getClassModel(type.getName());
        return model.elementStream()
                .filter(e -> e instanceof MethodModel)
                .map(MethodModel.class::cast)
                // Every serializable lambda in a class is re-invoked from a switch in this one
                // synthetic method, to be able to invoke it again on deserialization. Its
                // invokedynamic call sites are the same ones already reached through the code that
                // actually creates each lambda - walking it too would parse every lambda body a
                // second time for nothing, tagged with this method's own line (the class
                // declaration)
                .filter(m -> !m.methodName().stringValue().equals("$deserializeLambda$"))
                .flatMap(CompoundElement::elementStream)
                .filter(e -> e instanceof CodeModel)
                .map(CodeModel.class::cast)
                .flatMap(CompoundElement::elementStream)
                .filter(el -> el instanceof LineNumber || el instanceof InvokeDynamicInstruction)
                .map(el -> validate(el, fileName))
                .filter(Objects::nonNull)
                .toList();
    }

    private Result validate(CodeElement el, String fileName) {
        switch (el) {
            case LineNumber ln -> lineNumber = ln.line();
            case InvokeDynamicInstruction id -> {
                try {
                    invokeDynamic(id);
                    return new Ok(fileName, lineNumber);
                } catch (BytecodeParseException e) {
                    return new Error(fileName, e.line(), e.getMessage(), e);
                } catch (Exception e) {
                    return new Error(fileName, lineNumber,
                            format("Uncaught %s while parsing lambda (%s). BUG!",
                                    e.getClass().getSimpleName(), e.getMessage()), e);
                }
            }
            default -> {
            }
        }
        return null;
    }

    private void invokeDynamic(InvokeDynamicInstruction idi) throws DecoderException {
        if (idi.opcode() != Opcode.INVOKEDYNAMIC) {
            throw new BytecodeParseException(lineNumber, format("Unsupported opcode %s", idi.opcode()));
        }
        var bsm = idi.bootstrapMethod();
        var bsmMethod = bsm.methodName();
        // Check if it's a lambda metafactory
        if (bsm.owner().equals(ClassDesc.of("java.lang.invoke.LambdaMetafactory"))
                && (bsmMethod.equals("metafactory") || bsmMethod.equals("altMetafactory"))) {
            var args = idi.bootstrapArgs();
            if (args.size() >= 3) {
                var implHandle = (DirectMethodHandleDesc) args.get(1); // Handle to lambda body
                Method method;
                try {
                    // Flags (if altMetafactory)
                    var flags = (args.size() > 3) ? (Integer) args.get(3) : 0;
                    var isSerializable = (flags & LambdaMetafactory.FLAG_SERIALIZABLE) != 0;
                    if (isSerializable) {
                        var lookup = MethodHandles.privateLookupIn(classForDesc(implHandle.owner()), MethodHandles.lookup());
                        method = lookup.revealDirect(implHandle.resolveConstantDesc(lookup)).reflectAs(Method.class, lookup);
                        // The indy call site's parameters are the captured values, so its count
                        // is how many of the body's leading parameters the lambda captured.
                        var code = parseBytecode(findCodeModel(method), idi.typeSymbol().parameterCount());
                        // A predicate (boolean return) is consumed by ClauseBuilder, which fully
                        // supports a bare boolean expression, a list (for in()/notIn()), and more -
                        // only a value-producing lambda (anything else) is ever handed to
                        // ColumnResolver, which refuses those same shapes unconditionally. Only this
                        // call site - which alone knows what kind of lambda it just found - can tell
                        // the two apart; BytecodeParser decodes without caring which one it is.
                        if (method.getReturnType() != boolean.class) {
                            var reason = reasonNotSelectable(resolve(code));
                            if (reason.isPresent()) {
                                throw new BytecodeParseException(lineNumber,
                                        format("%s cannot be used as a query value", reason.get()));
                            }
                        }
                    }
                } catch (ReflectiveOperationException e) {
                    throw new BytecodeParseException(lineNumber, e.getMessage(), e);
                }
            }
        }
    }

    private ClassModel getClassModel(String name) {
        var classResourcePath = name.replace('.', '/') + ".class";
        try {
            return ClassFile.of().parse(getClassBytes(classResourcePath));
        } catch (IOException e) {
            throw new ClassDecodingException(format("Error loading class %s (%s)", name, e.getMessage()), e);
        }
    }

    private ClassModel getClassModel(Method method) throws DecoderException {
        return getClassModel(method.getDeclaringClass().getName());
    }

    private byte[] getClassBytes(String classResourcePath) throws IOException {
        try (var inputStream = loader.getResourceAsStream(classResourcePath);
             var byteStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException(format("Class file for %s not found as a resource", classResourcePath));
            }
            var buffer = new byte[1024];
            var bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
            }
            return byteStream.toByteArray();
        }
    }

    private Class<?> classForDesc(ClassDesc classDesc) throws DecoderException {
        try {
            if (classDesc.isPrimitive()) {
                return classDesc.resolveConstantDesc(MethodHandles.lookup());
            }
            var internalName = classDesc.descriptorString();  // e.g., "Ljava/lang/String;"
            var binaryName = internalName.replace('/', '.')
                    .replaceFirst("^L", "")
                    .replaceFirst(";$", "");
            return loader.loadClass(binaryName);
        } catch (ReflectiveOperationException e) {
            throw new ClassDecodingException(e.getMessage(), e);
        }
    }

    private CodeModel findCodeModel(Method method) throws DecoderException {
        return findCodeModel(getClassModel(method), method.getName());
    }

    private CodeModel findCodeModel(ClassModel classModel, String methodName) {
        var optM = classModel.methods().stream()
                .filter(method -> method.methodName().stringValue().equals(methodName))
                .findFirst();
        if (optM.isEmpty()) {
            throw new ClassDecodingException(format("Cannot find method model for method %s", methodName));
        }
        var optC = optM.get().code();
        if (optC.isEmpty()) {
            throw new ClassDecodingException(format("Cannot find value model for method %s", methodName));
        }
        return optC.get();
    }

    /**
     * Recursively loads all compiled classes from the output directory.
     */
    private List<Class<?>> loadAllClasses(File directory, ClassLoader classLoader, String packagePrefix) {
        var classes = new ArrayList<Class<?>>();
        if (!directory.exists() || !directory.isDirectory()) {
            return classes;
        }
        var files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        for (var file : files) {
            if (file.isDirectory()) {
                // Recursively process subdirectories
                var newPackage = packagePrefix.isEmpty() ? file.getName() : packagePrefix + "." + file.getName();
                classes.addAll(loadAllClasses(file, classLoader, newPackage));
            } else if (file.getName().endsWith(".class")) {
                // Load the class
                var className = file.getName().substring(0, file.getName().length() - 6); // Remove .class
                var fullClassName = packagePrefix.isEmpty() ? className : packagePrefix + "." + className;
                try {
                    var clazz = Class.forName(fullClassName, true, classLoader);
                    classes.add(clazz);
                    getLog().debug("Loaded class: " + fullClassName);
                } catch (ClassNotFoundException e) {
                    getLog().warn("Failed to load class: " + fullClassName, e);
                }
            }
        }
        return classes;
    }

    private sealed interface Result permits Ok, Error {
        String fileName();

        int lineNumber();
    }

    private record Ok(String fileName, int lineNumber) implements Result {
    }

    /**
     * @param e the exception the failure was reported as, kept for a future improvement that
     *          wants its stack trace or cause chain - not currently shown to the user.
     */
    private record Error(String fileName, int lineNumber, String error, Exception e) implements Result {

        // Two reports of the same problem at the same place are one error, even if each was
        // thrown as its own exception instance - equals()/hashCode() are overridden so e (which
        // Exception does not give value semantics) does not defeat the errors set's deduplication.
        @Override
        public boolean equals(Object o) {
            return o instanceof Error other
                    && lineNumber == other.lineNumber
                    && fileName.equals(other.fileName)
                    && error.equals(other.error);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileName, lineNumber, error);
        }
    }


}

