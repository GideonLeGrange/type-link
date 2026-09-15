package me.legrange.typelink.sql.parser;

import me.legrange.typelink.TableMapper;

import java.util.List;

/**
 * One scope's worth of what a value needs to resolve against.
 *
 * @param mapper the table mapper
 * @param args   captured values of the lambda being read
 * @param types  the tables this scope reads, in the order a row parameter's position counts them
 * @param outer  the scope around this one, or null at the outermost. A correlated sub-select refers
 *               to a row of an enclosing scope, and that row's position counts that scope's tables,
 *               not this one's.
 */
@SuppressWarnings("rawtypes")
record Context(TableMapper mapper, List<?> args, List<Class<?>> types, Context outer) {

    Context(TableMapper mapper, List<?> args, List<Class<?>> types) {
        this(mapper, args, types, null);
    }

    /** This scope, or the one {@code depth} steps out, or null if the nesting does not go that far. */
    Context at(int depth) {
        var scope = this;
        for (var i = 0; i < depth && scope != null; i++) {
            scope = scope.outer();
        }
        return scope;
    }

    /** The same scope with different captured values, keeping what encloses it. */
    Context withArgs(List<?> args) {
        return new Context(mapper, args, types, outer);
    }

    /** A scope nested inside this one. */
    Context nest(List<?> args, List<Class<?>> types) {
        return new Context(mapper, args, types, this);
    }
}
