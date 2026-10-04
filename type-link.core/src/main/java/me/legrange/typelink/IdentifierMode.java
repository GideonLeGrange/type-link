package me.legrange.typelink;

/**
 * How table and column names are written into generated SQL.
 *
 * <p>Quoted identifiers are case-sensitive on most databases and unquoted ones are folded (to lower
 * case on PostgreSQL, upper case on H2 and Oracle), so the right mode depends on how the schema was
 * created.
 */
public enum IdentifierMode {

    /**
     * Quote only names that cannot be written bare: SQL reserved words (such as a {@code Group}
     * table) and names that are not plain letters, digits and underscores. Whoever created such a
     * table had to quote it, so quoting it again matches exactly, and names that already work
     * unquoted are left alone. The default.
     *
     * <p>The reserved-word list is deliberately conservative - words reserved by every major
     * database - so that a name is never quoted where it could safely have been bare. A word that is
     * reserved by only one database (such as {@code KEY} or {@code INDEX} on MariaDB) is not
     * recognised; use {@link #ALWAYS} if your schema has such names.
     */
    AUTO,

    /**
     * Quote every table and column name, using exactly the case the mapper reports. For schemas
     * created with quoted, exact-case names.
     */
    ALWAYS,

    /** Write names exactly as the mapper reports them. */
    NEVER
}
