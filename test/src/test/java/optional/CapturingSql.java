package optional;

import me.legrange.typelink.Database;
import me.legrange.typelink.sql.structure.SqlQuery;
import me.legrange.typelink.sql.generator.SqlGenerator;

import java.util.List;

/** Builds the SQL for {@link Reminder} queries without running them. */
final class CapturingSql {

    private SqlQuery captured;

    private final Database<Record> database = new Database<>(new OptionalMapper()) {
        @Override
        protected List<List<?>> query(SqlQuery query) {
            captured = query;
            return List.of();
        }
    };

    Database<Record> database() {
        return database;
    }

    me.legrange.typelink.From1<Reminder> from() {
        return database.from(Reminder.class);
    }

    String sql() {
        return SqlGenerator.generate(captured).sql();
    }

    List<Object> params() {
        return SqlGenerator.generate(captured).params();
    }
}
