package me.legrange.typelink.sql.parser;

public final class QueryParseException extends RuntimeException {

    QueryParseException(String message) {
        super(message);
    }

    QueryParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
