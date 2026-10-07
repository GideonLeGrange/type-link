package rec;

/** Every boxed type that can be null, and a primitive, over a table whose columns can all be NULL. */
public record Gauge(Long id, Integer count, Long total, Double ratio, Boolean flag, Short small, Float weight,
                    Byte tiny, Character grade, int plainCount) {
}
