package decoding;

import me.legrange.typelink.BeanMapper;
import me.legrange.typelink.Database;
import me.legrange.typelink.TableMapper;
import me.legrange.typelink.sql.generator.SqlGenerator;
import me.legrange.typelink.sql.structure.SqlQuery;

import java.util.List;

/**
 * {@link CapturingDatabase} for beans rather than records.
 *
 * <p>Records cannot extend one another, so anything about how a column's table is decided when a
 * lambda is declared over a supertype needs a class hierarchy, and a mapper that accepts one.
 */
final class CapturingBeanDatabase<B> extends Database<B> {

    private SqlQuery captured;

    CapturingBeanDatabase(Class<B> superType) {
        super(new BeanMapper<>(superType));
    }

    /** For tests that need a mapper of their own - one that names columns per type, say. */
    CapturingBeanDatabase(Class<B> superType, TableMapper<B> mapper) {
        super(mapper);
    }

    @Override
    protected List<List<?>> query(SqlQuery query) {
        this.captured = query;
        return List.of();
    }

    /** The SQL generated for the query most recently built against this database. */
    String sql() {
        if (captured == null) {
            throw new IllegalStateException("No query was built against this database");
        }
        return SqlGenerator.generate(captured).sql();
    }

    /** The bind parameters generated for that query, in order. */
    List<Object> params() {
        if (captured == null) {
            throw new IllegalStateException("No query was built against this database");
        }
        return SqlGenerator.generate(captured).params();
    }

    /** Just the WHERE, for assertions that are only about how a column was qualified. */
    String where() {
        return sql().replaceAll(".*WHERE ", "");
    }
}
