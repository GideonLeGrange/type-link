package me.legrange.typelink.builder;

import me.legrange.typelink.Database;
import me.legrange.typelink.IdentifierMode;
import me.legrange.typelink.SqlDatabase;
import me.legrange.typelink.TableMapper;

import java.sql.Connection;
import java.util.function.Supplier;

public final class WithMapper<O> {

    private final TableMapper<O> mapper;
    private final Supplier<Connection> connectionSupplier;
    private final IdentifierMode identifierMode;

    WithMapper(Supplier<Connection> connectionSupplier, TableMapper<O> mapper) {
        this(connectionSupplier, mapper, IdentifierMode.AUTO);
    }

    private WithMapper(Supplier<Connection> connectionSupplier, TableMapper<O> mapper, IdentifierMode identifierMode) {
        this.connectionSupplier = connectionSupplier;
        this.mapper = mapper;
        this.identifierMode = identifierMode;
    }

    /**
     * How table and column names are written into the generated SQL. The default is
     * {@link IdentifierMode#AUTO}: quote only names that cannot be written bare, such as reserved
     * words.
     */
    public WithMapper<O> identifiers(IdentifierMode mode) {
        return new WithMapper<>(connectionSupplier, mapper, mode);
    }

    /** Shorthand for {@code identifiers(IdentifierMode.ALWAYS)}. */
    public WithMapper<O> quoteIdentifiers() {
        return identifiers(IdentifierMode.ALWAYS);
    }

    public Database<O> build() {
        return new SqlDatabase<>(connectionSupplier, mapper, identifierMode);
    }
}
