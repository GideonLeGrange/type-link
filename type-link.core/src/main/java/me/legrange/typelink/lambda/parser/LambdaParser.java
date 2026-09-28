package me.legrange.typelink.lambda.parser;

import me.legrange.typelink.lambda.DecoderException;
import me.legrange.typelink.lambda.structure.Lambda;

import java.io.Serializable;

import static me.legrange.typelink.lambda.parser.ClassUtil.findCodeModel;
import static me.legrange.typelink.lambda.parser.ClassUtil.getCapturedArguments;

public final class LambdaParser {

    private LambdaParser() {
    }

    public static Lambda parse(Serializable function) {
        var model = findCodeModel(function);
        try {
            var args = getCapturedArguments(function);
            var flow = BytecodeParser.parseBytecode(model, args.size());
            return new Lambda(BytecodeParser.resolve(flow), args);
        } catch (DecoderException e) {
            System.out.println("--- Byte value that failed ---");
            BytecodePrinter.printModel(model);
            throw e;
        }
    }

}