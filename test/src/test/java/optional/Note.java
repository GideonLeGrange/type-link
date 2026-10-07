package optional;

/** Points at a {@link Reminder}, which may not exist, so a left join from here yields a reminder with no row. */
public record Note(long id, long reminderId, String text) {
}
