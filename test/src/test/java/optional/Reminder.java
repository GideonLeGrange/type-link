package optional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * A row whose {@code due} column may be NULL, in the shape ObjDB gives a {@code @nullable} field: the stored
 * value is a plain {@link LocalDate}, but the getter people write queries against returns an {@link Optional}.
 */
public record Reminder(long id, String label, LocalDate due) {

    public Optional<LocalDate> getDue() {
        return Optional.ofNullable(due);
    }
}
