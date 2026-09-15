package me.legrange.typelink;

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

    public SqlDatabase(Supplier<Connection> connectionSupplier, TableMapper<O> mapper) {
        super(mapper);
        this.connectionSupplier = connectionSupplier;
    }

    public List<List<?>> query(SqlQuery query) throws SQLException {
        return readFromSql(generate(query), query.select().columns());
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
