package me.legrange.typelink.lambda;

public final class ClassDecodingException extends DecoderException {
    public ClassDecodingException(String message) {
        super(message);
    }

    public ClassDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
