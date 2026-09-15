package me.legrange.typelink.builder;

import me.legrange.typelink.Database;
import me.legrange.typelink.SqlDatabase;
import me.legrange.typelink.TableMapper;

import java.sql.Connection;
import java.util.function.Supplier;

public final class WithMapper<O> {

    private final TableMapper<O> mapper;
    private final Supplier<Connection> connectionSupplier;

    WithMapper(Supplier<Connection> connectionSupplier, TableMapper<O> mapper) {
        this.connectionSupplier = connectionSupplier;
        this.mapper = mapper;
    }

    public Database<O> build() {
        return new SqlDatabase<>(connectionSupplier, mapper);
    }
}
