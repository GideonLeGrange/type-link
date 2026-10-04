package me.legrange.typelink;

import me.legrange.typelink.sql.generator.IdentifierQuoter;
import me.legrange.typelink.sql.generator.SqlFragment;
import me.legrange.typelink.sql.structure.SqlColumn;
import me.legrange.typelink.sql.structure.SqlQuery;
import me.legrange.typelink.sql.unpack.Readers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static me.legrange.typelink.sql.generator.SqlGenerator.generate;

public final class SqlDatabase<O> extends Database<O>{

    private final Supplier<Connection> connectionSupplier;
    private final IdentifierMode identifierMode;
    private volatile IdentifierQuoter quoter;

    public SqlDatabase(Supplier<Connection> connectionSupplier, TableMapper<O> mapper) {
        this(connectionSupplier, mapper, IdentifierMode.AUTO);
    }

    /**
     * @param identifierMode how table and column names are written into SQL; see {@link IdentifierMode}
     */
    public SqlDatabase(Supplier<Connection> connectionSupplier, TableMapper<O> mapper, IdentifierMode identifierMode) {
        super(mapper);
        this.connectionSupplier = connectionSupplier;
        this.identifierMode = identifierMode;
    }

    public List<List<?>> query(SqlQuery query) throws SQLException {
        return readFromSql(generate(query, quoter()), query.select().columns());
    }

    private IdentifierQuoter quoter() throws SQLException {
        if (identifierMode == IdentifierMode.NEVER) {
            return IdentifierQuoter.NONE;
        }
        var q = quoter;
        if (q == null) {
            try (var con = getConnection()) {
                q = IdentifierQuoter.forMode(identifierMode, con.getMetaData().getIdentifierQuoteString());
            }
            quoter = q;
        }
        return q;
    }

    private List<List<?>> readFromSql(SqlFragment fragment, SqlColumn column) throws SQLException {
        try (var con = getConnection();
             var stmt = con.prepareStatement(fragment.sql())) {
            bind(stmt, fragment.params());
            try (var rs = stmt.executeQuery()) {
                var readers = Readers.getReaders(rs, mapper(), column);
                var raw = new ArrayList<List<?>>();
                while (rs.next()) {
                    raw.add(readers.stream().map(reader -> reader.unpack(rs)).toList());
                }
                return raw;
            }
        }
    }

    private static void bind(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (var i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }

    private Connection getConnection() {
        return connectionSupplier.get();
    }

}
