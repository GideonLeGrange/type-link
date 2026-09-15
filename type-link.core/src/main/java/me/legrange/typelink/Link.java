package me.legrange.typelink;

import me.legrange.typelink.lambda.parser.LambdaParser;
import me.legrange.typelink.lambda.structure.Lambda;

import java.io.Serializable;

public abstract sealed class Link permits AndClause, DistinctClause, FromClause, GroupClause, HavingClause, JoinClause, LimitClause, Link1, Link2, Link3, OrClause, OrderClause, SelectionLink, WhereClause {

    private final Link left;
    private final Serializable function;
    private final Lambda lambda;

    protected Link(Link left, Serializable function) {
        this.left = left;
        this.function = function;
        lambda = parse(function);
    }

    protected Link(Link left, Lambda lambda) {
        this.left = left;
        this.lambda = lambda;
        this.function = null;
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
