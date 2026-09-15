package me.legrange.typelink.sql.unpack;

public final class UnpackException extends RuntimeException {

    public UnpackException(String message) {
        super(message);
    }

    public UnpackException(String message, Throwable cause) {
        super(message, cause);
    }
}
