package me.legrange.typelink.lambda;

public final class BytecodeParseException extends DecoderException {

    private final int line;

    public BytecodeParseException(int line, String message) {
        super(message);
        this.line = line;
    }

    public BytecodeParseException(int line, String message, Throwable cause) {
        super(message, cause);
        this.line = line;
    }

    public int line() {
        return line;
    }
}
