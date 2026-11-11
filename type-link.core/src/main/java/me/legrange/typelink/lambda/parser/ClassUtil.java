package me.legrange.typelink.lambda.parser;

import me.legrange.typelink.lambda.ClassDecodingException;
import me.legrange.typelink.lambda.DecoderException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeModel;
import java.lang.classfile.constantpool.FieldRefEntry;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.lang.String.format;

 final class ClassUtil {

    /**
     * What has been read of each class. It is kept per {@link Class}, not per class name: when an application reloads its
     * classes (Quarkus in dev mode does, after an edit) the new class has the same name as the old one but is a different
     * Class, so it is read again and a lambda is never decoded from what the class used to say. What is kept for a class goes
     * when the class is unloaded. A ClassValue is also safe to use from many threads.
     *
     * <p>A lambda is deliberately not cached anywhere: a lambda that captures something is a new object every time a query
     * is built, so a cache of lambdas only ever grows, and keeps everything the lambdas captured alive.
     */
    private static final ClassValue<ClassModel> models = new ClassValue<>() {
        @Override
        protected ClassModel computeValue(Class<?> type) {
            var classResourcePath = type.getName().replace('.', '/') + ".class";
            try {
                return ClassFile.of().parse(getClassBytes(type, classResourcePath));
            } catch (IOException e) {
                throw new ClassDecodingException(format("Error loading class %s (%s)", type.getName(), e.getMessage()), e);
            }
        }
    };

    private ClassUtil() {
    }

    static Class<?> classForDesc(ClassDesc classDesc) throws DecoderException {
        try {
            if (classDesc.isPrimitive()) {
                return classDesc.resolveConstantDesc(MethodHandles.lookup());
            }
            var internalName = classDesc.descriptorString();  // e.g., "Ljava/lang/String;"
            var binaryName = internalName.replace('/', '.')
                    .replaceFirst("^L", "")
                    .replaceFirst(";$", "");
            return Thread.currentThread().getContextClassLoader().loadClass(binaryName); // Ensure the class is loaded
//            return Class.forName(binaryName);
        } catch (ReflectiveOperationException e) {
            throw new ClassDecodingException(e.getMessage(), e);
        }
    }

    static Field fieldForRef(FieldRefEntry field) throws DecoderException {
        var name = field.name().stringValue();
        var owner = classForDesc(field.owner().asSymbol());
        // The reference names the type the code read through, which for an inherited field is not
        // the type that declares it - getDeclaredField alone does not look up the hierarchy.
        for (var type = owner; type != null; type = type.getSuperclass()) {
            try {
                return type.getDeclaredField(name);
            } catch (NoSuchFieldException _) {
                // keep walking
            }
        }
        throw new ClassDecodingException(format("No field %s on %s or any of its supertypes",
                name, owner.getName()));
    }

    static Object constForDesc(ConstantDesc constDesc) throws DecoderException {
        // A class-literal constant must resolve through classForDesc: resolveConstantDesc below
        // binds to this method's own lookup, i.e. type-link.core's classloader, which cannot see
        // a consumer project's classes.
        if (constDesc instanceof ClassDesc classDesc) {
            return classForDesc(classDesc);
        }
        try {
            return constDesc.resolveConstantDesc(MethodHandles.lookup());
        } catch (ReflectiveOperationException e) {
            throw new ClassDecodingException(e.getMessage(), e);
        }
    }

    static CodeModel findCodeModel(Serializable serializable) throws DecoderException {
        return findCodeModel(toSerializedLambda(serializable));
    }

    static CodeModel findCodeModel(SerializedLambda lambda) throws DecoderException {
        return findCodeModel(models.get(classNamed(lambda.getImplClass())), lambda.getImplMethodName());
    }

    static CodeModel findCodeModel(Method method) throws DecoderException {
        return findCodeModel(models.get(method.getDeclaringClass()), method);
    }

    /**
     * The method a serializable lambda refers to, if it is a plain reference to an instance method ({@code Type::name})
     * rather than a lambda written out. A lambda's body is code compiled for it, marked synthetic; a method reference
     * points at a method that exists in its own right. A reference that captures something, or names a static method
     * or constructor, is not what a mapper could take for a column, so it is not reported.
     */
    static Optional<Method> methodReferenceTarget(Serializable function) {
        return methodReferenceTarget(toSerializedLambda(function));
    }

    static Optional<Method> methodReferenceTarget(SerializedLambda lambda) {
        var kind = lambda.getImplMethodKind();
        if (lambda.getCapturedArgCount() != 0
                || (kind != MethodHandleInfo.REF_invokeVirtual && kind != MethodHandleInfo.REF_invokeInterface)) {
            return Optional.empty();
        }
        try {
            var owner = Thread.currentThread().getContextClassLoader().loadClass(lambda.getImplClass().replace('/', '.'));
            return Arrays.stream(owner.getDeclaredMethods())
                    .filter(method -> method.getName().equals(lambda.getImplMethodName()))
                    .filter(method -> MethodType.methodType(method.getReturnType(), method.getParameterTypes())
                            .toMethodDescriptorString().equals(lambda.getImplMethodSignature()))
                    .filter(method -> !method.isSynthetic())
                    .findFirst();
        } catch (ClassNotFoundException e) {
            throw new ClassDecodingException(e.getMessage(), e);
        }
    }

    static List<Object> getCapturedArguments(Serializable function) throws DecoderException {
        return getCapturedArguments(toSerializedLambda(function));
    }

    static List<Object> getCapturedArguments(SerializedLambda lambda) {
        return IntStream.range(0, lambda.getCapturedArgCount())
                .mapToObj(lambda::getCapturedArg)
                .toList();
    }

    private static CodeModel findCodeModel(ClassModel classModel, Method method) throws DecoderException {
        return findCodeModel(classModel, method.getName());
    }

    private static CodeModel findCodeModel(ClassModel classModel, String methodName) {
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

    /** The class that a lambda says it was written in (it names it with slashes), as the application's loader sees it */
    private static Class<?> classNamed(String internalName) {
        var name = internalName.replace('/', '.');
        var loader = Thread.currentThread().getContextClassLoader();
        try {
            return loader != null ? loader.loadClass(name) : Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new ClassDecodingException(format("Error loading class %s (%s)", name, e.getMessage()), e);
        }
    }

    /** Not kept: see {@link #models} */
    static SerializedLambda toSerializedLambda(Serializable lambda) throws DecoderException {
        try {
            var method = lambda.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return (SerializedLambda) method.invoke(lambda);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new ClassDecodingException(format("Error serializing lambda (%s)", e.getMessage()), e);
        }
    }

    /**
     * Load the byte value for the given class
     *
     * @return The byte value
     * @throws IOException thrown if the class data cannot be loaded
     */
    private static byte[] getClassBytes(Class<?> type, String classResourcePath) throws IOException {
        try (var inputStream = openClassResource(type, classResourcePath);
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

    /**
     * Open the class file of the given class: from the loader of the class itself first, since that is the loader that read
     * this very version of it (which matters when the application reloads its classes), then from the thread's context class
     * loader, which in environments such as Quarkus can see application classes that this library's own loader cannot (it is
     * on a parent loader, application code on a child), and last from this library's own loader.
     */
    private static InputStream openClassResource(Class<?> type, String classResourcePath) {
        // the class's own loader first: it is the one that read this very class, so it cannot give another version of it
        var own = type.getClassLoader();
        if (own != null) {
            var stream = own.getResourceAsStream(classResourcePath);
            if (stream != null) {
                return stream;
            }
        }
        var contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            var stream = contextLoader.getResourceAsStream(classResourcePath);
            if (stream != null) {
                return stream;
            }
        }
        return ClassUtil.class.getClassLoader().getResourceAsStream(classResourcePath);
    }

}
