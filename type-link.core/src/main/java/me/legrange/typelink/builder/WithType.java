package me.legrange.typelink.builder;

import java.sql.Connection;
import java.util.function.Supplier;

public final class WithType<O> {

    private final Class<O>  type;

     WithType(Class<O> type) {
        this.type = type;
    }

    public WithConnectionSupplier<O> connection(Supplier<Connection> connectionSupplier) {
        return new WithConnectionSupplier<>( connectionSupplier);
    }

    Class<O> type() {
        return type;
    }
}
