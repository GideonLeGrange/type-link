package rec;

/** A single nullable character column, isolated from the richer fixtures so its edge cases stay easy to set up. */
public record CharRow(Long id, Character grade) {
}
