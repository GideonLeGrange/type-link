package optional;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Experimental. Documents what happens today when a lambda calls {@code Optional} methods on a getter that maps to
 * a nullable column. Each test states the SQL we would want; they fail until {@code Optional} is understood.
 * <p>
 * Scope agreed so far: {@code isPresent}, {@code isEmpty} and {@code get}. {@code orElse} and {@code map} are out.
 */
class Test_7000_OptionalGetterFilters {

    private static final String FROM = " FROM Reminder";

    private static String where(CapturingSql db) {
        var sql = db.sql();
        var at = sql.indexOf(" WHERE ");
        assertTrue(at >= 0, "no WHERE clause in: " + sql);
        return sql.substring(at + " WHERE ".length());
    }

    @Test
    void isPresentIsNotNull() {
        var db = new CapturingSql();
        db.from().where(r -> r.getDue().isPresent()).list();

        assertEquals("Reminder.due IS NOT NULL", where(db));
    }

    @Test
    void isEmptyIsNull() {
        var db = new CapturingSql();
        db.from().where(r -> r.getDue().isEmpty()).list();

        assertEquals("Reminder.due IS NULL", where(db));
    }

    @Test
    void notIsPresentIsNull() {
        var db = new CapturingSql();
        db.from().where(r -> !r.getDue().isPresent()).list();

        assertEquals("Reminder.due IS NULL", where(db));
    }

    @Test
    void getPassesThroughToTheColumn() {
        var db = new CapturingSql();
        var cutoff = LocalDate.of(2026, 10, 1);
        db.from().where(r -> r.getDue().get().isAfter(cutoff)).list();

        assertEquals("Reminder.due > ?", where(db));
        assertEquals(List.of(cutoff), db.params());
    }

    @Test
    void isPresentGuardsGet() {
        var db = new CapturingSql();
        var cutoff = LocalDate.of(2026, 10, 1);
        db.from().where(r -> r.getDue().isPresent() && r.getDue().get().isAfter(cutoff)).list();

        assertEquals("Reminder.due IS NOT NULL AND Reminder.due > ?", where(db));
    }

    @Test
    void optionalGetterInOrderBy() {
        var db = new CapturingSql();
        db.from().orderBy(r -> r.getDue().get()).list();

        assertTrue(db.sql().endsWith("ORDER BY Reminder.due"), db.sql());
    }
}
