package me.legrange.typelink.sql.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * A piece of generated SQL text paired with the ordered list of values that must be bound to the
 * {@code ?} placeholders it contains.
 *
 * <p>{@link SqlGenerator} builds a query bottom-up out of fragments instead of raw strings so that
 * every value coming from user data (predicate constants, {@code IN} lists, {@code LIKE} patterns, ...)
 * is carried as a bound parameter rather than being spliced into the SQL text. This is what makes the
 * generated SQL safe to execute with a {@link java.sql.PreparedStatement}.
 *
 * <p>Fragments compose left-to-right with {@link #plus(SqlFragment)}: the resulting {@code sql} is the
 * textual concatenation, and {@code params} is the concatenation of both parameter lists in the same
 * order, so the position of each {@code ?} in the final SQL always lines up with its value.
 */
public record SqlFragment(String sql, List<Object> params) {

    public static final SqlFragment EMPTY = new SqlFragment("", List.of());

    public static SqlFragment text(String sql) {
        return sql.isEmpty() ? EMPTY : new SqlFragment(sql, List.of());
    }

    /** A single bound parameter, rendered as one {@code ?} placeholder. */
    public static SqlFragment param(Object value) {
        var params = new ArrayList<Object>(1);
        params.add(value);
        return new SqlFragment("?", params);
    }

    public SqlFragment plus(SqlFragment other) {
        if (other.params.isEmpty()) {
            return other.sql.isEmpty() ? this : new SqlFragment(sql + other.sql, params);
        }
        if (params.isEmpty()) {
            return sql.isEmpty() ? other : new SqlFragment(sql + other.sql, other.params);
        }
        var combined = new ArrayList<Object>(params.size() + other.params.size());
        combined.addAll(params);
        combined.addAll(other.params);
        return new SqlFragment(sql + other.sql, combined);
    }

    /** Joins fragments with {@code delimiter}, with no prefix or suffix. */
    public static SqlFragment join(List<SqlFragment> parts, String delimiter) {
        return join(parts, delimiter, "", "");
    }

    /** Joins fragments with {@code delimiter}, wrapped in {@code prefix}/{@code suffix}. */
    public static SqlFragment join(List<SqlFragment> parts, String delimiter, String prefix, String suffix) {
        var result = text(prefix);
        for (var i = 0; i < parts.size(); i++) {
            if (i > 0) {
                result = result.plus(text(delimiter));
            }
            result = result.plus(parts.get(i));
        }
        return result.plus(text(suffix));
    }
}
