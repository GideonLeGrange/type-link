package database.rec;

import database.testing.DatabaseTest;
import database.testing.TestDatabase;
import org.junit.jupiter.api.TestTemplate;
import rec.Person;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests guarding against SQL injection via values captured in query lambdas.
 * {@link me.legrange.typelink.sql.generator.SqlGenerator} must bind every value as a
 * {@code PreparedStatement} parameter rather than splicing it into the generated SQL text.
 */
@SuppressWarnings("NewClassNamingConvention")
public final class Test_9000_SqlInjection extends DatabaseTest {

    @TestTemplate
    public void classicTautologyPayloadMustNotMatchEverything(TestDatabase testDb) throws SQLException {
        // If this value were spliced into the SQL text instead of bound as a parameter, this would
        // produce `WHERE Person.name = '' OR '1'='1'` - a tautology matching every row.
        var result = from(testDb, Person.class)
                .where(p -> p.name().equals("' OR '1'='1"))
                .list();
        assertThat(result).isEmpty();
    }

    @TestTemplate
    public void payloadInLikeClauseIsTreatedAsData(TestDatabase testDb) throws SQLException {
        var result = from(testDb, Person.class)
                .where(p -> p.name().contains("'; DROP TABLE Person; --"))
                .list();
        assertThat(result).isEmpty();
    }

    @TestTemplate
    public void payloadInInClauseIsTreatedAsData(TestDatabase testDb) throws SQLException {
        var result = from(testDb, Person.class)
                .where(p -> java.util.List.of("' OR '1'='1", "Bob Jones'; --").contains(p.name()))
                .list();
        assertThat(result).isEmpty();
    }
}
