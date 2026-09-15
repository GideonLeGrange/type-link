package decoding;

import me.legrange.typelink.Database;
import me.legrange.typelink.TableMapper;
import me.legrange.typelink.sql.generator.SqlGenerator;
import me.legrange.typelink.sql.structure.SqlQuery;

import java.util.List;

/**
 * A {@link Database} that executes nothing and remembers the query it was handed.
 *
 * <p>The decoding tests in this package are about what the lambda decoder makes of a predicate,
 * not about what a database does with the result, so they assert on generated SQL and bind
 * parameters instead of on rows. That keeps them fast and free of any database dependency -
 * unlike the tests under {@code database}, nothing here needs a container or an H2 instance.
 *
 * <p>Build a query against this as normal and call {@link #sql()} or {@link #params()}:
 *
 * {@snippet :
 * var db = new CapturingDatabase();
 * db.from(Invoice.class).where(i -> i.paid()).list();
 * assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.paid = ?", db.sql());
 * }
 */
final class CapturingDatabase extends Database<Record> {

    private SqlQuery captured;

    CapturingDatabase() {
        this(TableMapper.RECORD_MAPPER);
    }

    /** For tests that need a mapper of their own - one that renames a table, say. */
    CapturingDatabase(TableMapper<Record> mapper) {
        super(mapper);
    }

    @Override
    protected List<List<?>> query(SqlQuery query) {
        this.captured = query;
        return List.of();
    }

    /** The SQL generated for the query most recently built against this database. */
    String sql() {
        return SqlGenerator.generate(requireCaptured()).sql();
    }

    /** The bind parameters generated for that query, in order. */
    List<Object> params() {
        return SqlGenerator.generate(requireCaptured()).params();
    }

    /** The query most recently built against this database, unrendered. */
    SqlQuery captured() {
        return requireCaptured();
    }

    private SqlQuery requireCaptured() {
        if (captured == null) {
            throw new IllegalStateException("No query was built against this database");
        }
        return captured;
    }
}
