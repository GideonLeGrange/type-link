package me.legrange.typelink;

import me.legrange.typelink.sql.structure.SqlQuery;

import java.sql.SQLException;
import java.util.List;

import static me.legrange.typelink.sql.parser.QueryParser.parseChain;
import static me.legrange.typelink.sql.unpack.Unpack.unpack;

public abstract class Database<O> {

    private final TableMapper<O> mapper;

    protected Database(TableMapper<O> mapper) {
        this.mapper = mapper;
    }

    public final  <T1 extends O> From1<T1> from(Class<T1> type) {
        return new FromLink1<>(this, type);
    }

    public final <T1 extends O, T2 extends O> From2<T1, T2> from(Class<T1> type1, Class<T2> type2) {
        return new FromLink2<>(this, type1, type2);
    }

    public final <T1 extends O, T2 extends O, T3 extends O> From3<T1, T2, T3> from(Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        return new FromLink3<>(this, type1, type2, type3);
    }

    final <T> List<T> list(SelectionLink link) {
        try {
            //noinspection unchecked
            return (List<T>) query(link);
        } catch (SQLException e) {
            throw new QueryException(e.getMessage(), e);
        }
    }

    final <T> T object(SelectionLink link) {
        try {
            //noinspection unchecked
            return (T) query(link).getFirst();
        } catch (SQLException e) {
            throw new QueryException(e.getMessage(), e);
        }
    }

    private List<?> query(SelectionLink link) throws SQLException {
        return unpack(query(parseChain(link, mapper)), mapper, link);
    }

    protected abstract List<List<?>> query(SqlQuery query) throws SQLException;

    protected final TableMapper<O> mapper() {
        return mapper;
    }

}
