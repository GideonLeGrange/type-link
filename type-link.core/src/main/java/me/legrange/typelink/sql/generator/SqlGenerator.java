package me.legrange.typelink.sql.generator;

import me.legrange.typelink.sql.structure.*;
import me.legrange.typelink.sql.structure.SqlLikeOperator.Wildcard;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static me.legrange.typelink.sql.generator.SqlFragment.join;
import static me.legrange.typelink.sql.generator.SqlFragment.text;

/**
 * Turns a {@link SqlQuery} into an executable {@link SqlFragment}: SQL text with {@code ?}
 * placeholders and the ordered list of values to bind to them.
 *
 * <p>Every value that originates from application/user data (predicate constants, {@code IN} list
 * members, {@code LIKE} patterns, ...) is carried as a bound parameter rather than being written
 * into the SQL text, so the result is safe to run through a {@link java.sql.PreparedStatement}
 * without risking SQL injection. Only structural SQL (table/column names, keywords, and integer
 * {@code LIMIT}/{@code OFFSET} values, which cannot carry SQL syntax) is emitted as literal text.
 */
public final class SqlGenerator {

    private final IdentifierQuoter quoter;

    private SqlGenerator(IdentifierQuoter quoter) {
        this.quoter = quoter;
    }

    /** Generates SQL with identifiers exactly as the mapper reported them. */
    public static SqlFragment generate(SqlQuery query) {
        return generate(query, IdentifierQuoter.NONE);
    }

    /** Generates SQL with every table and column name passed through the quoter. */
    public static SqlFragment generate(SqlQuery query, IdentifierQuoter quoter) {
        return new SqlGenerator(quoter).generateQuery(query);
    }

    private SqlFragment generateQuery(SqlQuery query) {
        return text("SELECT ")
                .plus(select(query.select()))
                .plus(from(query.from()))
                .plus(joins(query.joins()))
                .plus(where(query.where()))
                .plus(groupBy(query.groupBy()))
                .plus(having(query.having()))
                .plus(order(query.order()))
                .plus(limit(query.limit()));
    }

    private SqlFragment select(SqlSelect select) {
        return (select.distinct() ? text("DISTINCT ") : SqlFragment.EMPTY).plus(column(select.columns()));
    }

    public SqlFragment column(SqlColumn select) {
        return switch (select) {
            case SqlFunction sqlFunction -> function(sqlFunction);
            case SqlOperation sqlOperation -> operation(sqlOperation);
            case SqlTable sqlTable -> table(sqlTable);
            case SqlTableColumn sqlTableColumn -> column(sqlTableColumn);
            case SqlAll(List<SqlColumn> list) -> switch (list.size()) {
                case 0 -> text("*");
                case 1 -> column(list.getFirst());
                default -> join(list.stream().map(this::column).toList(), ", ");
            };
            case SqlConstant sqlConstant -> sqlConstant(sqlConstant);
            case SqlSubSelect sqlSubSelect -> sqlSubSelect(sqlSubSelect);
            case SqlConcat sqlConcat -> sqlConcat(sqlConcat);
        };
    }

    private SqlFragment sqlSubSelect(SqlSubSelect subSelect) {
        return text("(").plus(generateQuery(subSelect.select())).plus(text(")"));
    }

    private SqlFragment sqlConcat(SqlConcat concat) {
        return join(concat.parameters().stream().map(this::column).toList(), ", ", "CONCAT(", ")");
    }

    private SqlFragment function(SqlFunction function) {
        return switch (function) {
            case SqlCount sqlCount -> count(sqlCount);
            case SqlSum sqlSum -> sum(sqlSum);
            case SqlAvg sqlAvg -> avg(sqlAvg);
            case SqlMin sqlMin -> min(sqlMin);
            case SqlMax sqlMax -> max(sqlMax);
        };
    }

    private SqlFragment count(SqlCount count) {
        return text("COUNT(")
                .plus(switch (count.parameter()) {
                    case SqlAll _, SqlTable _ -> text("*");
                    default -> column(count.parameter());
                })
                .plus(text(")"));
    }

    private SqlFragment sum(SqlSum sum) {
        return text("SUM(").plus(column(sum.parameter())).plus(text(")"));
    }

    private SqlFragment min(SqlMin min) {
        return text("MIN(").plus(column(min.parameter())).plus(text(")"));
    }

    private SqlFragment max(SqlMax max) {
        return text("MAX(").plus(column(max.parameter())).plus(text(")"));
    }

    private SqlFragment avg(SqlAvg avg) {
        return text("AVG(").plus(column(avg.parameter())).plus(text(")"));
    }

    private SqlFragment operation(SqlOperation operation) {
        return switch (operation) {
            case SqlAdd(SqlColumn left, SqlColumn right) -> column(left).plus(text(" + ")).plus(column(right));
            case SqlSubtract(SqlColumn left, SqlColumn right) -> column(left).plus(text(" - ")).plus(column(right));
            case SqlMultiply(SqlColumn left, SqlColumn right) -> column(left).plus(text(" * ")).plus(column(right));
            case SqlDivide(SqlColumn left, SqlColumn right) -> column(left).plus(text(" / ")).plus(column(right));
        };
    }

    private SqlFragment sqlPart(SqlPart part) {
        return switch (part) {
            case SqlTableColumn sqlColumn -> column(sqlColumn);
            case SqlValue sqlValue -> sqlValue(sqlValue);
            case SqlClause sqlClause -> clause(sqlClause);
            case SqlColumn sqlSelect -> column(sqlSelect);
        };
    }

    private SqlFragment sqlValue(SqlValue sqlValue) {
        return switch (sqlValue) {
            case SqlConstant sqlConstant -> sqlConstant(sqlConstant);
            case SqlList sqlList -> sqlList(sqlList);
        };
    }

    private SqlFragment sqlList(SqlList list) {
        return join(list.values().stream().map(this::sqlConstant).toList(), ", ", "(", ")");
    }

    private SqlFragment sqlConstant(SqlConstant sqlConstant) {
        return sqlConstant.value() == null ? SqlFragment.param(null) : value(sqlConstant.value());
    }

    /**
     * Binds a literal value as a parameter rather than splicing it into the SQL text. Enum constants
     * are bound by name (as they would be compared/stored), and a bare {@link Collection} constant
     * (e.g. a {@code List} field captured by a {@code .contains(...)} check) is expanded into a
     * parenthesised, individually-bound list, mirroring how {@link SqlList} is rendered. The set of
     * types accepted here matches what the query side has always supported.
     */
    private SqlFragment value(Object value) {
        return switch (value) {
            case Integer _, Long _, Double _, Float _, Boolean _, String _, LocalDate _, LocalDateTime _, Date _ ->
                    SqlFragment.param(value);
            case Short _, Byte _, BigDecimal _, LocalTime _ -> SqlFragment.param(value);
            // drivers disagree about BigInteger; a DECIMAL column takes a BigDecimal everywhere
            case BigInteger bigInteger -> SqlFragment.param(new BigDecimal(bigInteger));
            case Character c -> SqlFragment.param(String.valueOf(c));
            case Enum<?> e -> SqlFragment.param(e.name());
            case Collection<?> ls -> join(ls.stream().map(this::value).toList(), ", ", "(", ")");
            default ->
                    throw new SqlGenerateException(format("Unexpected value of type %s. BUG!", value.getClass().getSimpleName()));
        };
    }

    /**
     * A table's columns, each written out by name in the order the mapper lists them.
     *
     * <p>{@code Table.*} would leave the order to the database, and readers find a column by its
     * position in the select list. Naming them is also what lets two queries select the same
     * shape, which a {@code UNION} needs of its arms.
     */
    private SqlFragment table(SqlTable table) {
        if (table.columns().isEmpty()) {
            return text(quoter.quote(table.table().name()) + ".*");
        }
        return join(table.columns().stream().map(this::column).toList(), ", ");
    }

    private SqlFragment column(SqlTableColumn column) {
        return text(quoter.quote(column.tableName()) + "." + quoter.quote(column.name()));
    }

    private SqlFragment from(SqlFrom from) {
        return text(" FROM " + from.tables().stream().map(ref -> quoter.quote(ref.name())).collect(Collectors.joining(", ")));
    }

    private SqlFragment joins(SqlJoin sqlJoin) {
        var result = SqlFragment.EMPTY;
        for (var j : sqlJoin.joins()) {
            result = result
                    .plus(text(switch (j) {
                        case SqlFullOuterJoin _ -> " FULL OUTER";
                        case SqlInnerJoin _ -> " INNER";
                        case SqlLeftOuterJoin _ -> " LEFT OUTER";
                        case SqlRightOuterJoin _ -> " RIGHT OUTER";
                    } + " JOIN " + quoter.quote(j.table().name()) + " ON "))
                    .plus(clause(j.on()));
        }
        return result;
    }

    private SqlFragment clause(SqlClause clause) {
        return switch (clause) {
            case SqlLogicalOperator sqlLogicalOperator -> logicalOperator(sqlLogicalOperator);
            case SqlNot not -> not(not);
            case SqlNull sqlNull -> sqlNull(sqlNull);
            case SqlRelationalOperator sqlOp -> relationalOperator(sqlOp);
            case SqlSubSelect sqlSubSelect -> text("(").plus(generateQuery(sqlSubSelect.select())).plus(text(")"));
        };
    }

    private SqlFragment not(SqlNot not) {
        return text("NOT ").plus(clause(not.clause()));
    }

    private SqlFragment sqlNull(SqlNull sqlNull) {
        return switch (sqlNull) {
            case SqlIsNotNull(SqlTableColumn column) -> column(column).plus(text(" IS NOT NULL"));
            case SqlIsNull(SqlTableColumn column) -> column(column).plus(text(" IS NULL"));
            case SqlNull _ -> text("");
        };
    }

    private SqlFragment relationalOperator(SqlRelationalOperator operator) {
        return switch (operator) {
            case SqlLikeOperator sqlLikeOperator -> sqlLikeOperator(sqlLikeOperator);
            case SqlSetOperator sqlSetOperator -> sqlSetOperator(sqlSetOperator);
            case SqlSimpleOperator sqlSimpleOperator -> sqlSimpleOperator(sqlSimpleOperator);
        };
    }

    private SqlFragment sqlLikeOperator(SqlLikeOperator operator) {
        return sqlPart(operator.left())
                .plus(text(" " + switch (operator) {
                    case SqlLike _ -> "LIKE ";
                    case SqlNotLike _ -> "NOT LIKE ";
                }))
                .plus(operator.right() instanceof SqlConstant sqlConstant
                        ? likeConstant(operator.wildcard(), sqlConstant)
                        : sqlPart(operator.right()));
    }

    private SqlFragment likeConstant(Wildcard wildcard, SqlConstant constant) {
        if (!(constant.value() instanceof String s)) {
            return sqlConstant(constant);
        }
        return SqlFragment.param(switch (wildcard) {
            case LEFT -> "%" + s;
            case RIGHT -> s + "%";
            case BOTH -> "%" + s + "%";
        });
    }

    private SqlFragment sqlSetOperator(SqlSetOperator operator) {
        if (isEmptySet(operator.right())) {
            // "IN ()" is a syntax error in most databases: nothing is in an empty set, everything is not in it
            return text(operator instanceof SqlInSet ? "1 = 0" : "1 = 1");
        }
        return sqlPart(operator.left())
                .plus(text(" " + switch (operator) {
                    case SqlInSet _ -> " IN ";
                    case SqlNotInSet _ -> "NOT IN ";
                }))
                .plus(sqlPart(operator.right()));
    }

    private static boolean isEmptySet(Object right) {
        return switch (right) {
            case SqlList list -> list.values().isEmpty();
            case SqlConstant constant -> constant.value() instanceof Collection<?> values && values.isEmpty();
            default -> false;
        };
    }

    private SqlFragment sqlSimpleOperator(SqlSimpleOperator operator) {
        return sqlPart(operator.left())
                .plus(text(" " + switch (operator) {
                    case SqlEq _ -> "=";
                    case SqlGe _ -> ">=";
                    case SqlGt _ -> ">";
                    case SqlLe _ -> "<=";
                    case SqlLt _ -> "<";
                    case SqlNeq _ -> "<>";
                } + " "))
                .plus(sqlPart(operator.right()));
    }

    private SqlFragment nest(SqlLogicalOperator parent, SqlClause clause) {
        if (mustNest(parent, clause)) {
            return text("(").plus(clause(clause)).plus(text(")"));
        }
        return clause(clause);
    }

    private static boolean mustNest(SqlLogicalOperator op, SqlClause clause) {
        return (op instanceof SqlAnd && clause instanceof SqlOr) || (op instanceof SqlOr && clause instanceof SqlAnd);
    }

    private SqlFragment logicalOperator(SqlLogicalOperator operator) {
        return switch (operator) {
            case SqlAnd and -> nest(and, and.left()).plus(text(" AND ")).plus(nest(and, and.right()));
            case SqlOr or -> nest(or, or.left()).plus(text(" OR ")).plus(nest(or, or.right()));
        };
    }

    private SqlFragment where(SqlWhere where) {
        return where.clauses().isEmpty()
                ? text("")
                : text(" WHERE ").plus(join(where.clauses().stream().map(this::clause).toList(), " AND "));
    }

    private SqlFragment groupBy(SqlGroupBy groupBy) {
        return groupBy.groupBy().isEmpty()
                ? text("")
                : text(" GROUP BY ").plus(join(groupBy.groupBy().stream().map(this::column).toList(), ", "));
    }

    private SqlFragment having(SqlHaving having) {
        return having.clause().isEmpty()
                ? text("")
                : text(" HAVING ").plus(join(having.clause().stream().map(this::clause).toList(), " AND "));
    }

    private SqlFragment order(SqlOrder order) {
        return order.order().isEmpty()
                ? text("")
                : text(" ORDER BY ").plus(join(order.order().stream()
                        .map(clause -> column(clause.column()).plus(text(clause.reverse() ? " DESC" : "")))
                        .toList(), ", "));
    }

    /**
     * {@code LIMIT}/{@code OFFSET} values are always {@code int}s supplied directly as Java method
     * arguments (see {@code Limit1.limit(int, int)} and friends) — they cannot carry SQL syntax, so
     * they're safe to emit as literal text without going through a bound parameter.
     */
    private SqlFragment limit(SqlLimit limit) {
        return switch (limit) {
            case SqlLimited limited -> text(format(" LIMIT %d OFFSET %d", limited.limit(), limited.offset()));
            case SqlNotLimited _ -> text("");
        };
    }
}
