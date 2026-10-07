package me.legrange.typelink;

import me.legrange.typelink.lambda.parser.LambdaParser;
import me.legrange.typelink.lambda.structure.Lambda;
import me.legrange.typelink.sql.parser.LambdaValues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract sealed class Link permits AndClause, DistinctClause, FromClause, GroupClause, HavingClause, JoinClause, LimitClause, Link1, Link2, Link3, OrClause, OrderClause, SelectionLink, WhereClause {

    private final Link left;
    private final Serializable function;
    private final Lambda lambda;

    protected Link(Link left, Serializable function) {
        this.left = left;
        this.function = function;
        lambda = resolve(left, parse(function));
    }

    protected Link(Link left, Lambda lambda) {
        this.left = left;
        this.lambda = lambda;
        this.function = null;
    }

    /**
     * A method reference means a column or a body depending on the mapping. The mapper and the tables are known here,
     * so it is settled once, and every later stage sees the same kind of value a written-out lambda gives.
     */
    private static Lambda resolve(Link left, Lambda lambda) {
        if (lambda == null || left == null) {
            return lambda;
        }
        return LambdaValues.resolve(left.database().mapper(), left.tables(), lambda);
    }

    private static Lambda parse(Serializable function) {
        if (function == null) {
            return null;
        }
        if (function instanceof Selector<?> selector) {
            return parse(selector.getFunction());
        }
        return LambdaParser.parse(function);
    }

    /** The tables of the query as far as this link, in the order they appear in FROM and JOIN. */
    public final List<Class<?>> tables() {
        var res = new ArrayList<Class<?>>();
        var link = this;
        do {
            if (link instanceof FromLink from) {
                res.addAll(from.types());
            } else if (link instanceof JoinLink join) {
                res.add(join.type());
            }
            link = link.left();
        } while (link != null);
        return res.reversed();
    }

    public final Link left() {
        return left;
    }

    protected Database database() {
        return left.database();
    }

    Serializable function() {
        return function;
    }

    public Lambda lambda() {
        return lambda;
    }

}
