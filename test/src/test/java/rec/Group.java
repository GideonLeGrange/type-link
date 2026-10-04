package rec;

/** A record whose name is an SQL reserved word, to exercise identifier quoting. */
public record Group(Long id, String name) {
}
