package me.legrange.typelink.sql.parser;

import me.legrange.typelink.*;
import me.legrange.typelink.JoinLink.Type;
import me.legrange.typelink.lambda.structure.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static me.legrange.typelink.sql.parser.Values.intValue;

/**
 * Reconstructs a {@link Link} chain from a {@link MethodCall} tree.
 * This is used to parse sub-queries that appear as lambda expressions
 * (e.g. a nested {@code db.from(...).where(...)} inside a select).
 */
final class SubQueryParser {

    private SubQueryParser() {
    }

    static Link buildChain(MethodCall mc) {
        var left = switch (mc.target()) {
            case MethodCall call -> buildChain(call);
            case Argument _ -> null;
            default ->
                    throw new QueryParseException(format("Don't know how to find value for %s. BUG!", mc.target().type().getSimpleName()));
        };
        return switch (mc.method().getName()) {
            case "from" -> valueForFrom(mc.parameters());
            case "where" -> valueForWhere(left, mc.parameters());
            case "and" -> valueForAnd(left, mc.parameters());
            case "or" -> valueForOr(left, mc.parameters());
            case "join" -> valueForJoin(left, Type.INNER, mc.parameters());
            case "leftJoin" -> valueForJoin(left, Type.LEFT_OUTER, mc.parameters());
            case "rightJoin" -> valueForJoin(left, Type.RIGHT_OUTER, mc.parameters());
            case "fullJoin" -> valueForJoin(left, Type.FULL_OUTER, mc.parameters());
            case "groupBy" -> valueForGroup(left, mc.parameters());
            case "having" -> valueForHaving(left, mc.parameters());
            case "orderBy", "thenBy" -> valueForOrder(left, false, mc.parameters());
            case "orderByDescending", "thenByDescending" -> valueForOrder(left, true, mc.parameters());
            case "limit" -> valueForLimit(left, mc.parameters());
            case "distinct" -> new DistinctClause(left);
            case "list" -> valueForList(left, mc.parameters());
            case "max" -> valueForMax(left, mc.parameters());
            case "min" -> valueForMin(left, mc.parameters());
            case "avg" -> valueForAvg(left, mc.parameters());
            case "sum" -> valueForSum(left, mc.parameters());
            case "count" -> valueForCount(left, mc.parameters());
            default -> throw new QueryParseException(format("Don't know how to call '%s'. BUG!", mc.method().getName()));
        };
    }

    private static Link valueForFrom(List<Value> params) {
        var types = new ArrayList<Class<?>>();
        for (var param : params) {
            if (param instanceof Constant<?>(Class<?> type)) {
                types.add(type);
            } else {
                throw new QueryParseException(format("Don't know how to extract type from %s. BUG!", param.getClass().getSimpleName()));
            }
        }
        return new FromClause(types);
    }

    private static Link valueForWhere(Link left, List<Value> params) {
        if (params.size() != 1) {
            throw new QueryParseException("Expected one clause for where. BUG!");
        }
        var clause = params.getFirst();
        if (!(clause instanceof Expression expr)) {
            throw new QueryParseException("Expected expression in where clause. BUG!");
        }
        return new WhereClause(left, expr);
    }

    private static Link valueForAnd(Link left, List<Value> params) {
        if (params.size() != 1) {
            throw new QueryParseException("Expected one clause for and. BUG!");
        }
        var clause = params.getFirst();
        if (!(clause instanceof Expression expr)) {
            throw new QueryParseException("Expected expression in and clause. BUG!");
        }
        return new AndClause(left, expr);
    }

    private static Link valueForOr(Link left, List<Value> params) {
        if (params.size() != 1) {
            throw new QueryParseException("Expected one clause for or. BUG!");
        }
        var clause = params.getFirst();
        if (!(clause instanceof Expression expr)) {
            throw new QueryParseException("Expected expression in or clause. BUG!");
        }
        return new OrClause(left, expr);
    }

    private static Link valueForJoin(Link left, Type joinType, List<Value> params) {
        var second = params.getLast();
        if (!(second instanceof Constant<?>(Class<?> type))) {
            throw new QueryParseException("Expected type in join clause. BUG!");
        }
        var clause = params.getFirst();
        if (!(clause instanceof Expression expr)) {
            throw new QueryParseException("Expected expression in join clause. BUG!");
        }
        return new JoinClause(left, type, joinType, expr);
    }

    private static Link valueForGroup(Link left, List<Value> params) {
        if (params.isEmpty()) {
            throw new QueryParseException("Expected at least one clause for group. BUG!");
        }
        for (var clause : params) {
            left = new GroupClause(left, clause);
        }
        return left;
    }

    private static Link valueForHaving(Link left, List<Value> params) {
        if (params.size() != 1) {
            throw new QueryParseException("Expected one clause for having. BUG!");
        }
        var clause = params.getFirst();
        if (!(clause instanceof Expression expr)) {
            throw new QueryParseException("Expected expression in having clause. BUG!");
        }
        return new HavingClause(left, expr);
    }

    private static Link valueForOrder(Link left, boolean reversed, List<Value> params) {
        if (params.size() != 1) {
            throw new QueryParseException("Expected one clause for order. BUG!");
        }
        var clause = params.getFirst();
        return new OrderClause(left, clause, reversed);
    }

    private static Link valueForLimit(Link left, List<Value> parameters) {
        return switch (parameters.size()) {
            case 1 -> new LimitClause(left, intValue(parameters.getFirst()), 0);
            case 2 -> new LimitClause(left, intValue(parameters.getFirst()), intValue(parameters.getLast()));
            default -> throw new QueryParseException("Expected limit and possibly offset in limit clause. BUG!");
        };
    }

    private static ValueSelectionLink valueForList(Link left, List<Value> params) {
        if (params.isEmpty()) {
            return new ValueSelectionLink(left, null);
        }
        ValueSelectionLink res = null;
        for (var param : params) {
            res = new ValueSelectionLink(res != null ? res : left, param);
        }
        return res;
    }

    private static Link valueForMax(Link left, List<Value> params) {
        return new ValueSelectionLink(left, new Max(params.getFirst()));
    }

    private static Link valueForMin(Link left, List<Value> params) {
        return new ValueSelectionLink(left, new Min(params.getFirst()));
    }

    private static Link valueForAvg(Link left, List<Value> params) {
        return new ValueSelectionLink(left, new Avg(params.getFirst()));
    }

    private static Link valueForSum(Link left, List<Value> params) {
        return new ValueSelectionLink(left, new Sum(params.getFirst()));
    }

    private static Link valueForCount(Link left, List<Value> params) {
        return new ValueSelectionLink(left, new Count(params.isEmpty() ? null : params.getFirst()));
    }
}

