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
import java.util.Set;

import static java.lang.String.format;
import static me.legrange.typelink.lambda.parser.BytecodeParser.parseBytecode;

/**
 * Maven Mojo for validating typelink queries.
 * Loads all compiled classes from the project so they can be evaluated.
 */
// FIXME this plugin contains copied code from ClassUtil. ClassUtil probably needs to be
// reworked to accept a class loader, or at least have some methods exposed.
@Mojo(name = "validate-queries", defaultPhase = LifecyclePhase.VALIDATE)
public class ValidateQueries extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private String outputDirectory;
    private ClassLoader loader;
    private String fileName;
    private int lineNumber;
    private final Set<Error> errors = new HashSet<>();

    public void execute() throws MojoExecutionException {
        var packaging = project.getPackaging();
        if ("pom".equals(packaging) || "maven-archetype".equals(packaging)) {
            // Skip processing
            return;
        }        getLog().info("Loading compiled classes from project...");
        try {
            loader = createProjectClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            var loadedClasses = loadAllClasses(new File(outputDirectory), loader, "");

            getLog().info(format("Processing  %d classes", loadedClasses.size()));
            for (var clazz : loadedClasses) {
                fileName = clazz.getCanonicalName().replace(".", "/") + ".java";
                validate(clazz);
            }
            if (!errors.isEmpty()) {
                for (var error : errors.stream()
                        .sorted(Comparator.comparing(Error::fileName).thenComparing(Error::lineNumber)).toList()) {
                    getLog().error(format("Unsupported lambda code at line %d in %s: %s",
                            error.lineNumber(), error.fileName(), error.error()));
                }
                getLog().error("");
                getLog().error("The most likely cause of these errors is one of the following:");
                getLog().error("");
                getLog().error("""
                • A query lambda, while looking as if it can map to valid SQL, generates an unexpected Java bytecode.
                If you feel this is the case, report it""" );
                getLog().error("""
                        • A query lambda cannot be represented in SQL. Review the lambda to ensure it is doing what
                         you want it to do, and if so, consider rewriting it in a way that is more likely to be supported.""");
                getLog().error("");
                throw new MojoFailureException(format("There %s %d lambda%s with errors. Queries will fail at runtime",
                        errors.size() == 1 ? "is" : "are",
                        errors.size(),
                        errors.size() == 1 ? "" : "s"));
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to load project classes", e);
        }
    }

    /**
     * Creates a URLClassLoader that includes the project's output directory.
     */
    private ClassLoader createProjectClassLoader() throws Exception {
        return new URLClassLoader(new URL[]{makeUrl(outputDirectory)}, Thread.currentThread().getContextClassLoader());
    }

    private URL makeUrl(String dir) throws MalformedURLException {
        var file = new File(dir);
        if (!file.exists()) {
            throw new IllegalStateException("Output directory does not exist: " + dir);
        }
        return file.toURI().toURL();
    }

    private void validate(Class<?> type) {
        var model = getClassModel(type.getCanonicalName());
        model.elementStream()
                .filter(e -> e instanceof MethodModel)
                .map(MethodModel.class::cast)
                .flatMap(CompoundElement::elementStream)
                .filter(e -> e instanceof CodeModel)
                .map(CodeModel.class::cast)
                .flatMap(CompoundElement::elementStream)
                .filter(el -> el instanceof LineNumber || el instanceof InvokeDynamicInstruction)
                .forEach(this::validate);
    }

    private void validate(CodeElement el) {
        switch (el) {
            case LineNumber ln -> lineNumber = ln.line();
            case InvokeDynamicInstruction id -> {
                try {
                    invokeDynamic(id);
                } catch (BytecodeParseException e) {
                    errors.add(new Error(fileName, e.line(), e.getMessage()));
                }
            }
            default -> {
            }
        }
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
                        parseBytecode(findCodeModel(method), idi.typeSymbol().parameterCount());
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

    private record Error(String fileName, int lineNumber, String error) {
    }


}

