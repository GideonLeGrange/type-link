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
import java.lang.classfile.constantpool.Utf8Entry;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static me.legrange.typelink.lambda.parser.BytecodeParser.parseBytecode;
import static me.legrange.typelink.lambda.parser.BytecodeParser.resolve;
import static me.legrange.typelink.sql.parser.UnrepresentableValues.reasonNotSelectable;

/**
 * Maven Mojo for validating typelink queries.
 * Loads all compiled classes from the project so they can be evaluated.
 */
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

    /**
     * The only interfaces a lambda has to implement to be one of this library's query lambdas -
     * read from {@link SelectFunction} and {@link QueryPredicate}'s own {@code permits} clause
     * rather than listed by hand, so a leaf added to either sealed hierarchy later (a
     * {@code SelectFunction4}, say) is picked up here automatically instead of silently going
     * unvalidated until someone remembers this list exists too. Checked against each
     * {@code invokedynamic} call site's own target type - not every serializable lambda in a
     * matching class is one of ours, and a class doing its own unrelated serialization (RPC,
     * events, whatever) can easily have others.
     */
    private static final Set<ClassDesc> QUERY_LAMBDA_INTERFACES = Stream.of(SelectFunction.class, QueryPredicate.class)
            .flatMap(sealedRoot -> Stream.of(sealedRoot.getPermittedSubclasses()))
            .map(Class::describeConstable)
            .flatMap(Optional::stream)
            .collect(Collectors.toUnmodifiableSet());

    /**
     * The same interfaces as {@link #QUERY_LAMBDA_INTERFACES}, as the internal names they appear
     * under in a constant pool ({@code "Lme/legrange/typelink/SelectFunction1;"} to
     * {@code "me/legrange/typelink/SelectFunction1"}) - for the cheap, class-wide pre-check in
     * {@link #referencesQueryLambda}, which has no call site descriptor to compare a {@link ClassDesc}
     * against yet, only raw constant pool strings.
     */
    private static final Set<String> QUERY_LAMBDA_INTERFACE_NAMES = QUERY_LAMBDA_INTERFACES.stream()
            .map(desc -> desc.descriptorString().substring(1, desc.descriptorString().length() - 1))
            .collect(Collectors.toUnmodifiableSet());

    public void execute() throws MojoExecutionException {
        var packaging = project.getPackaging();
        if ("pom".equals(packaging) || "maven-archetype".equals(packaging)) {
            // Skip processing
            return;
        }
        getLog().info("Scanning compiled classes from project...");
        try {
            loader = createProjectClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            var classNames = findAllClasses(new File(outputDirectory), "");

            getLog().info(format("Found %d classes", classNames.size()));
            var candidates = 0;
            for (var className : classNames) {
                var model = getClassModel(className);
                // Loading (let alone initialising) every class just to ask whether it builds a
                // type-link query would run the static initialiser of everything in the project.
                // A class that never mentions any of these six interfaces cannot possibly create a
                // lambda implementing one, so it is skipped before any of that.
                if (!referencesQueryLambda(model)) {
                    continue;
                }
                candidates++;
                var fileName = topLevelBinaryName(className).replace('.', '/') + ".java";
                results.addAll(validate(model, fileName));
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
            getLog().info(format("%d of %d classes reference a query lambda; evaluated %d lambdas, %d OK and %d errors",
                    candidates, classNames.size(), results.size(),
                    results.stream().filter(result -> result instanceof Ok).count(),
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
     * A nested class lives in its enclosing class's .java file, not one of its own - and a binary
     * name says so without needing the class loaded: {@code Outer$Inner$Innermost} nests under
     * {@code Outer}, whatever comes before its first {@code $}.
     */
    private String topLevelBinaryName(String binaryName) {
        var dollar = binaryName.indexOf('$');
        return dollar < 0 ? binaryName : binaryName.substring(0, dollar);
    }

    /**
     * Whether {@code model}'s constant pool mentions any of the six interfaces
     * ({@code SelectFunction1/2/3}, {@code QueryPredicate1/2/3}) a lambda must implement to be one
     * of this library's query lambdas. A class that names none of them cannot invoke one of the
     * fluent methods that takes one, and so cannot contain a lambda worth decoding - checked before
     * paying to walk every method's instructions for one that was never going to be there.
     */
    private boolean referencesQueryLambda(ClassModel model) {
        for (var entry : model.constantPool()) {
            if (entry instanceof Utf8Entry utf8
                    && QUERY_LAMBDA_INTERFACE_NAMES.stream().anyMatch(utf8.stringValue()::contains)) {
                return true;
            }
        }
        return false;
    }

    private URL makeUrl(String dir) throws MalformedURLException {
        var file = new File(dir);
        if (!file.exists()) {
            throw new IllegalStateException("Output directory does not exist: " + dir);
        }
        return file.toURI().toURL();
    }

    private List<Result> validate(ClassModel model, String fileName) {
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
                    if (!invokeDynamic(id)) {
                        return null;
                    }
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

    /**
     * @return whether {@code idi} builds one of this library's own query lambdas at all - false
     * for anything else an ordinary class does with {@code invokedynamic} (string concatenation,
     * an unrelated lambda, a method reference passed to a stream, another library's own
     * serializable functional interface), none of which this method touches.
     */
    private boolean invokeDynamic(InvokeDynamicInstruction idi) throws DecoderException {
        var bsm = idi.bootstrapMethod();
        var bsmMethod = bsm.methodName();
        if (!bsm.owner().equals(ClassDesc.of("java.lang.invoke.LambdaMetafactory"))
                || !(bsmMethod.equals("metafactory") || bsmMethod.equals("altMetafactory"))) {
            return false;
        }
        // The call site's own return type names the interface the lambda it creates implements -
        // checked before any reflection or decoding, so a class that happens to build both a
        // type-link query and, say, a Comparator or its own serializable event object only ever
        // has the query lambda actually looked at.
        if (!QUERY_LAMBDA_INTERFACES.contains(idi.typeSymbol().returnType())) {
            return false;
        }
        var args = idi.bootstrapArgs();
        if (args.size() < 3) {
            return false;
        }
        var implHandle = (DirectMethodHandleDesc) args.get(1); // Handle to lambda body
        try {
            // Flags (if altMetafactory)
            var flags = (args.size() > 3) ? (Integer) args.get(3) : 0;
            var isSerializable = (flags & LambdaMetafactory.FLAG_SERIALIZABLE) != 0;
            if (!isSerializable) {
                return false;
            }
            var lookup = MethodHandles.privateLookupIn(classForDesc(implHandle.owner()), MethodHandles.lookup());
            var method = lookup.revealDirect(implHandle.resolveConstantDesc(lookup)).reflectAs(Method.class, lookup);
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
            return true;
        } catch (ReflectiveOperationException e) {
            throw new BytecodeParseException(lineNumber, e.getMessage(), e);
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
     * Recursively collects the binary name of every compiled class under the output directory -
     * from the file listing alone, without loading (and so without initialising - running static
     * initialisers for the whole project) a single one of them.
     */
    private List<String> findAllClasses(File directory, String packagePrefix) {
        var names = new ArrayList<String>();
        if (!directory.exists() || !directory.isDirectory()) {
            return names;
        }
        var files = directory.listFiles();
        if (files == null) {
            return names;
        }
        for (var file : files) {
            if (file.isDirectory()) {
                var newPackage = packagePrefix.isEmpty() ? file.getName() : packagePrefix + "." + file.getName();
                names.addAll(findAllClasses(file, newPackage));
            } else if (file.getName().endsWith(".class")) {
                var className = file.getName().substring(0, file.getName().length() - 6); // Remove .class
                names.add(packagePrefix.isEmpty() ? className : packagePrefix + "." + className);
            }
        }
        return names;
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

