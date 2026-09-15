package me.legrange.typelink.sql.parser;

import me.legrange.typelink.*;
import me.legrange.typelink.lambda.parser.LambdaParser;
import me.legrange.typelink.sql.structure.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static me.legrange.typelink.sql.parser.ClauseBuilder.clause;
import static me.legrange.typelink.sql.parser.ColumnResolver.column;
import static me.legrange.typelink.sql.parser.ColumnResolver.tableRef;
import static me.legrange.typelink.sql.parser.ColumnResolver.columns;

/**
 * Top-level query parser that traverses a {@link Link} chain and assembles
 * a complete {@link SqlQuery} by delegating to specialised helpers:
 * <ul>
 *   <li>{@link ColumnResolver} — resolves values to SQL columns</li>
 *   <li>{@link ClauseBuilder} — translates expressions into SQL clauses</li>
 *   <li>{@link SubQueryParser} — reconstructs Link chains from sub-query method calls</li>
 *   <li>{@link Values} — evaluates reflective constant values</li>
 * </ul>
 */
public final class QueryParser {

    private QueryParser() {
    }

    /**
     * Parse a chain of query parts into a SQL query.
     *
     * @param chain  The chain of query parts
     * @param mapper The table mapper
     * @return The parsed SQL query
     */
    public static SqlQuery parseChain(Link chain, TableMapper<?> mapper) throws QueryParseException {
        return parseChain(chain, mapper, List.of());
    }

    /**
     * Parse a chain of query parts into a SQL query, resolving captured values against
     * {@code args}.
     *
     * <p>A sub-select is parsed with the captured arguments of the lambda that contains it. Its own
     * clauses are nested lambdas, which carry none of their own - what they captured was substituted
     * for the enclosing scope's values while the bytecode was read, and those are positions in this
     * list.
     *
     * @param chain  The chain of query parts
     * @param mapper The table mapper
     * @param args   Captured values of the enclosing lambda
     */
    public static SqlQuery parseChain(Link chain, TableMapper<?> mapper, List<?> args) throws QueryParseException {
        return parse(chain, list -> new Context(mapper, args, types(list)));
    }

    /**
     * Parse a sub-query, as a scope nested inside {@code enclosing}.
     *
     * <p>A correlated sub-select refers to a row of the scope around it, and that row's position
     * counts that scope's tables. Passing the enclosing scope is what lets it be found.
     */
    static SqlQuery parseChain(Link chain, Context enclosing) throws QueryParseException {
        return parse(chain, list -> enclosing.nest(enclosing.args(), types(list)));
    }

    private static SqlQuery parse(Link chain, java.util.function.Function<List<Link>, Context> scope)
            throws QueryParseException {
        var list = toList(chain);
        var context = scope.apply(list);
        return new SqlQuery(
                select(context, list),
                from(context, list),
                join(context, list),
                where(context, list),
                groupBy(context, list),
                having(context, list),
                order(context, list),
                limit(list)
        );
    }

    /**
     * Compile a predicate into the SQL clause it means for one table.
     *
     * <p>{@link #parseChain} turns a whole builder chain into a query. This is the smaller thing a
     * caller needs when it already has the query and wants one more condition for it - a rule that
     * says which rows of {@code type} may be read, say, to be combined into a {@code WHERE} or a
     * join's {@code ON}. The predicate is the same {@link QueryPredicate1} that would be passed to
     * {@code where(...)}, and resolves its columns against {@code type}.
     *
     * @param predicate the predicate to compile
     * @param type      the table its row parameter stands for
     * @param mapper    the table mapper
     * @return the clause the predicate means
     */
    public static SqlClause parsePredicate(QueryPredicate1<?> predicate, Class<?> type, TableMapper<?> mapper)
            throws QueryParseException {
        return clause(new Context(mapper, List.of(), List.of(type)), LambdaParser.parse(predicate));
    }

    private static List<Class<?>> types(List<?> list) {
        return list.stream()
                .filter(link -> link instanceof FromLink)
                .map(FromLink.class::cast)
                .flatMap(link -> link.types().stream())
                .toList();
    }

    private static SqlSelect select(Context context, List<Link> list) {
        return new SqlSelect(columns(context, list), list.stream().anyMatch(link -> link instanceof DistinctLink));
    }

    private static SqlFrom from(Context context, List<Link> list) throws QueryParseException {
        var opt = list.stream()
                .filter(link -> link instanceof FromLink)
                .map(FromLink.class::cast)
                .findFirst();
        if (opt.isEmpty()) {
            throw new QueryParseException("No FROM clause in query. BUG!");
        }
        return new SqlFrom(opt.get().types().stream().map(type -> tableRef(context, type)).toList());
    }

    private static SqlJoin join(Context context, List<Link> list) {
        return new SqlJoin(list.stream()
                .filter(link -> link instanceof JoinLink)
                .map(link -> (Link & JoinLink) link)
                .map(join -> (SqlJoinClause) switch (join.joinType()) {
                    case INNER -> new SqlInnerJoin(tableRef(context, join.type()), clause(context, join.lambda()));
                    case LEFT_OUTER -> new SqlLeftOuterJoin(tableRef(context, join.type()), clause(context, join.lambda()));
                    case RIGHT_OUTER -> new SqlRightOuterJoin(tableRef(context, join.type()), clause(context, join.lambda()));
                    case FULL_OUTER -> new SqlFullOuterJoin(tableRef(context, join.type()), clause(context, join.lambda()));
                })
                .toList());
    }

    private static SqlWhere where(Context context, List<Link> list) {
        var links = list.stream()
                .filter(link -> link instanceof WhereLink || link instanceof AndLink || link instanceof OrLink)
                .toList();
        return new SqlWhere(clauses(context, links));
    }

    private static List<SqlClause> clauses(Context context, List<Link> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        SqlClause result = null;
        for (var link : list) {
            if (result == null) {
                result = clause(context, link.lambda());
            } else {
                result = switch (link) {
                    case AndLink _ -> new SqlAnd(result, clause(context, link.lambda()));
                    case OrLink _ -> new SqlOr(result, clause(context, link.lambda()));
                    default -> throw new QueryParseException(format("Unexpected link type %s in where clause. BUG!", link.getClass().getSimpleName()));
                };
            }
        }
        return List.of(result);
    }

    private static SqlOrder order(Context context, List<Link> list) {
        return new SqlOrder(
                list.stream()
                        .filter(link -> link instanceof OrderByLink)
                        .map(OrderByLink.class::cast)
                        .map(orderBy -> new SqlColumnOrder(column(context, ((Link) orderBy).lambda()), orderBy.reversed()))
                        .toList());
    }

    private static SqlGroupBy groupBy(Context context, List<Link> list) {
        return new SqlGroupBy(
                list.stream()
                        .filter(link -> link instanceof GroupByLink)
                        .map(GroupByLink.class::cast)
                        .map(groupBy -> column(context, ((Link) groupBy).lambda()))
                        .toList());
    }

    private static SqlHaving having(Context context, List<Link> list) {
        return new SqlHaving(list.stream()
                .filter(link -> link instanceof HavingLink)
                .map(HavingLink.class::cast)
                .map(having -> clause(context, ((Link) having).lambda()))
                .toList());
    }

    private static SqlLimit limit(List<Link> list) {
        var opt = list.stream()
                .filter(link -> link instanceof LimitLink)
                .map(LimitLink.class::cast)
                .findFirst();
        if (opt.isEmpty()) {
            return new SqlNotLimited();
        }
        var limit = opt.get();
        return new SqlLimited(limit.limit(), limit.offset());
    }

    private static List<Link> toList(Link chain) {
        var link = chain;
        var res = new ArrayList<Link>();
        while (link != null) {
            res.add(link);
            link = link.left();
        }
        return res.reversed();
    }
}
