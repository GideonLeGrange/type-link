package me.legrange.typelink.lambda;

public abstract sealed class DecoderException extends RuntimeException permits ClassDecodingException, BytecodeParseException {

    public DecoderException(String message) {
        super(message);
    }

    public DecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
