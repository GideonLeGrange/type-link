package rec;

import java.time.Instant;

/** A single nullable {@link Instant} column, isolated from the richer fixtures to keep its own table simple. */
public record Schedule(Long id, Instant startsAt) {
}
