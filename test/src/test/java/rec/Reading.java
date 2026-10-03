package rec;

/**
 * A record with one component of every primitive type, so that primitive accessors other than int, double and boolean
 * are exercised by the query tests.
 */
public record Reading(Long id, String label, long bigNum, float ratio, short small, byte tiny, char grade, boolean flag) {
}
