package me.legrange.typelink.lambda.parser;

import me.legrange.typelink.lambda.structure.MethodReference;
import me.legrange.typelink.lambda.structure.Value;

import java.util.function.Supplier;

/**
 * A method reference's body, decoded the first time it is asked for. Whatever the decoding does, including failing,
 * happens once: a body that cannot be decoded fails the same way every time and is never retried.
 */
final class Memoized implements MethodReference.Body {

    private final Supplier<Value> decode;
    private Value value;
    private RuntimeException failure;
    private boolean done;

    Memoized(Supplier<Value> decode) {
        this.decode = decode;
    }

    @Override
    public synchronized Value get() {
        if (!done) {
            try {
                value = decode.get();
            } catch (RuntimeException e) {
                failure = e;
            }
            done = true;
        }
        if (failure != null) {
            throw failure;
        }
        return value;
    }
}
