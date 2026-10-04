package me.legrange.typelink.sql.generator;

import me.legrange.typelink.IdentifierMode;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Quotes SQL identifiers (table and column names) so that reserved words such as {@code Group} or
 * {@code Order}, and names with unusual characters, are valid in generated SQL.
 *
 * <p>The quote character is database specific (back-tick for MySQL/MariaDB, double quote for
 * PostgreSQL, H2, Oracle, SQLite, ...), so it is normally taken from the connection with
 * {@link java.sql.DatabaseMetaData#getIdentifierQuoteString()}.
 *
 * <p>A name containing dots is treated as a qualified name ({@code schema.table}) and each part is
 * quoted separately.
 */
@FunctionalInterface
public interface IdentifierQuoter {

    /** Leaves identifiers exactly as the mapper reported them. */
    IdentifierQuoter NONE = identifier -> identifier;

    String quote(String identifier);

    /**
     * Quotes every identifier. {@code quoteString} is as reported by
     * {@link java.sql.DatabaseMetaData#getIdentifierQuoteString()}; blank means the database does not
     * support quoting, in which case identifiers are left alone. A quote character inside an
     * identifier is escaped by doubling it.
     */
    static IdentifierQuoter of(String quoteString) {
        return create(quoteString, _ -> true);
    }

    /**
     * Quotes only identifiers that cannot be written bare: a name that is not a plain identifier
     * (letters, digits and underscores, not starting with a digit) or that is a reserved word on every
     * major database.
     */
    static IdentifierQuoter auto(String quoteString) {
        return create(quoteString, IdentifierQuoter::needsQuoting);
    }

    /** The quoter for a mode; {@code quoteString} is the database's identifier quote string. */
    static IdentifierQuoter forMode(IdentifierMode mode, String quoteString) {
        return switch (mode) {
            case NEVER -> NONE;
            case ALWAYS -> of(quoteString);
            case AUTO -> auto(quoteString);
        };
    }

    /** Whether a single identifier part must be quoted to be valid SQL. */
    static boolean needsQuoting(String part) {
        return !PLAIN.matcher(part).matches() || RESERVED.contains(part.toUpperCase(Locale.ROOT));
    }

    private static IdentifierQuoter create(String quoteString, java.util.function.Predicate<String> mustQuote) {
        if (quoteString == null || quoteString.isBlank()) {
            return NONE;
        }
        var quote = quoteString.trim();
        return identifier -> {
            var parts = identifier.contains(quote) ? new String[]{identifier} : identifier.split("\\.", -1);
            var out = new StringBuilder();
            for (var part : parts) {
                if (!out.isEmpty()) {
                    out.append('.');
                }
                out.append(mustQuote.test(part) ? quote + part.replace(quote, quote + quote) + quote : part);
            }
            return out.toString();
        };
    }

    Pattern PLAIN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * Words reserved by every major database (PostgreSQL, MySQL/MariaDB, H2, Oracle, SQL Server,
     * SQLite): the ones that make a bare identifier a syntax error everywhere. Kept conservative on
     * purpose - quoting a name the database would have accepted bare breaks schemas whose unquoted
     * names the database folded to another case.
     */
     Set<String> RESERVED = Set.of(
            "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "BETWEEN", "BY", "CASE", "CHECK", "COLUMN", "CONSTRAINT",
            "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEFAULT",
            "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXISTS", "FALSE", "FOR", "FOREIGN", "FROM", "FULL",
            "GRANT", "GROUP", "HAVING", "IN", "INNER", "INSERT", "INTERSECT", "INTO", "IS", "JOIN", "LEFT", "LIKE",
            "LIMIT", "NATURAL", "NOT", "NULL", "OFFSET", "ON", "OR", "ORDER", "OUTER", "PRIMARY", "REFERENCES",
            "RIGHT", "SELECT", "SET", "TABLE", "THEN", "TO", "TRUE", "UNION", "UNIQUE", "UPDATE", "USER", "USING",
            "VALUES", "WHEN", "WHERE", "WITH");
}
