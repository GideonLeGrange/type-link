package me.legrange.typelink.builder;

import me.legrange.typelink.TableMapper;

import java.sql.Connection;
import java.util.function.Supplier;

public final class WithConnectionSupplier<O> {

    private final Supplier<Connection> connectionSupplier;

    WithConnectionSupplier( Supplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    public WithMapper<O> mapper(TableMapper<O> mapper) {
        return new WithMapper<>(connectionSupplier, mapper);
    }

}
