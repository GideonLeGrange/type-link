package rec;

/** Points at a {@link Gauge}, or at one that does not exist. */
public record Meter(long id, long gaugeId) {
}
