package me.legrange.typelink.lambda.parser;

import me.legrange.typelink.lambda.DecoderException;
import me.legrange.typelink.lambda.structure.Lambda;
import me.legrange.typelink.lambda.structure.MethodReference;
import me.legrange.typelink.lambda.structure.Value;

import java.io.Serializable;

import static me.legrange.typelink.lambda.parser.ClassUtil.findCodeModel;
import static me.legrange.typelink.lambda.parser.ClassUtil.getCapturedArguments;
import static me.legrange.typelink.lambda.parser.ClassUtil.methodReferenceTarget;

public final class LambdaParser {

    private LambdaParser() {
    }

    /**
     * A lambda decodes to what its body does. A method reference, such as {@code Reminder::getDue}, has no body of its
     * own to read: it decodes to a {@link MethodReference} that carries the method and, when asked, the method's body.
     * Whether the method is a column or code to expand is for whoever knows the mapping to say.
     */
    public static Lambda parse(Serializable function) {
        var args = getCapturedArguments(function);
        var target = methodReferenceTarget(function);
        if (target.isPresent()) {
            return new Lambda(new MethodReference(target.get(), new Memoized(() -> body(function, args.size()))), args);
        }
        return new Lambda(body(function, args.size()), args);
    }

    private static Value body(Serializable function, int capturedCount) {
        var model = findCodeModel(function);
        try {
            return BytecodeParser.resolve(BytecodeParser.parseBytecode(model, capturedCount));
        } catch (DecoderException e) {
            System.out.println("--- Byte value that failed ---");
            BytecodePrinter.printModel(model);
            throw e;
        }
    }

}
