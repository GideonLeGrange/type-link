package me.legrange.typelink.sql.parser;

import me.legrange.typelink.*;
import me.legrange.typelink.lambda.DecoderException;
import me.legrange.typelink.lambda.structure.*;
import me.legrange.typelink.lambda.structure.And;
import me.legrange.typelink.lambda.structure.Or;
import me.legrange.typelink.sql.structure.*;


import static me.legrange.typelink.sql.parser.ColumnResolver.*;
import static java.lang.String.format;
import static me.legrange.typelink.sql.parser.SubQueryParser.buildChain;
import static me.legrange.typelink.sql.parser.Values.*;
import static me.legrange.typelink.sql.structure.SqlLikeOperator.Wildcard.*;

/**
 * Translates {@link Expression} and {@link Value} trees into SQL clauses ({@link SqlClause})
 * and SQL parts ({@link SqlPart}). Handles WHERE/HAVING/JOIN predicates: relational operators,
 * logical operators, wildcard/LIKE patterns, set membership, null checks, and value resolution.
 */
final class ClauseBuilder {

    private ClauseBuilder() {
    }

    static SqlClause clause(Context context, Lambda lambda) throws QueryParseException {
        var part = toSqlPart(context, lambda);
        return switch (part) {
            case SqlClause sqlClause -> sqlClause;
            case SqlColumn sqlColumn -> new SqlEq(sqlColumn, new SqlConstant(true));
            default -> throw new QueryParseException("Cannot parse SQL clause from lambda. BUG!");
        };
    }

    private static SqlPart toSqlPart(Context context, Lambda lc) throws DecoderException {
        // A nested lambda carries no captured arguments of its own - it was never serialized. What
        // it captured was substituted for the enclosing scope's values as the bytecode was read, so
        // it is the enclosing lambda's arguments those values are positions in.
        var args = lc.arguments().isEmpty() ? context.args() : lc.arguments();
        return value(context.withArgs(args), lc.value());
    }

    static SqlPart value(Context context, Value value) {
        if (isColumn(context, value)) {
            return column(context, value);
        }
        return switch (value) {
            case Argument arg -> argument(context, arg);
            case Constant<?>(Object val) -> new SqlConstant(val);
            case ListValue list -> list(context, list);
            case MethodCall methodCall -> valueForMethodCall(context, methodCall);
            case ConstructorCall _ -> throw new QueryParseException("Constructor calls not supported yet");
            case Operator operator -> operator(context, operator);
            case MethodReference ref -> valueForMethodReference(context, ref);
            case StaticMethodCall sm -> valueForStaticMethod(sm);
            case Expression expression -> expression(context, expression);
            case NewObject _ -> throw new QueryParseException("Cannot create SQL clause from object");
            case Reference _ -> throw new QueryParseException("Cannot create SQL clause from reference");
            case FieldReference fieldReference -> valueForFieldReference(context, fieldReference);
        };
    }

    // --- Value helpers ---

    private static SqlPart valueForFieldReference(Context context, FieldReference fieldReference) {
        return switch (fieldReference) {
            case StaticFieldReference fw -> fieldValue(fw);
            case InstanceFieldReference f -> new SqlConstant(fieldValue(context, f));
        };
    }


    private static SqlConstant argument(Context context, Argument arg) {
        return new SqlConstant(Values.capturedArgument(context, arg));
    }

    private static SqlList list(Context context, ListValue listValue) {
        return new SqlList(listValue.values().stream()
                .map(v -> value(context, v))
                .filter(v -> (v instanceof SqlConstant))
                .map(v -> (SqlConstant) v).toList());
    }

    private static SqlPart valueForMethodCall(Context context, MethodCall methodCall) {
        var target = methodCall.target();
        if (Clause.class.isAssignableFrom(target.type()) || Database.class.isAssignableFrom(target.type())) {
            return new SqlSubSelect(QueryParser.parseChain(buildChain(methodCall), context));
        }
        if (isColumn(context, target)) {
            return column(context, target);
        }
        return new SqlConstant(callMethod(context, methodCall));
    }

    private static SqlPart valueForStaticMethod(StaticMethodCall sm) {
        return new SqlConstant(callStaticMethod(sm));
    }

    private static SqlPart valueForMethodReference(Context context, MethodReference ref) {
        if (isColumn(context, ref)) {
            return column(context, ref);
        }
        throw new QueryParseException(String.format("Unexpected %s method target. BUG!", ref.getClass().getSimpleName()));
    }

    // --- Expression → SqlClause ---

    static SqlClause expression(Context context, Expression expr) {
        return switch (expr) {
            case LogicalOperator logicalOperator -> logicalOperator(context, logicalOperator);
            case Not not -> new SqlNot(expression(context, not.expression()));
            case NullFunction nullFunction -> nullOperator(context, nullFunction);
            case RelationalEvaluation relationalEvaluation -> relationalOperator(context, relationalEvaluation);
            case SetFunction setFunction -> setOperator(context, setFunction);
            case WildcardFunction wildcardFunction -> wildcardOperator(context, wildcardFunction);
            case DateFunction dateFunction -> functionOperator(context, dateFunction);
        };
    }

    private static SqlRelationalOperator functionOperator(Context context, DateFunction fo) {
        return switch (fo) {
            case IsAfter isAfter -> new SqlGt(column(context, isAfter.left()), value(context, isAfter.right()));
            case IsBefore isBefore -> new SqlLt(column(context, isBefore.left()), value(context, isBefore.right()));
        };
    }

    private static SqlLikeOperator wildcardOperator(Context context, WildcardFunction operator) {
        return switch (operator) {
            case Contains _ -> new SqlLike(column(context, operator.left()), value(context, operator.right()), BOTH);
            case EndsWith _ -> new SqlLike(column(context, operator.left()), value(context, operator.right()), LEFT);
            case NotContains _ ->
                    new SqlNotLike(column(context, operator.left()), value(context, operator.right()), BOTH);
            case NotEndsWith _ ->
                    new SqlNotLike(column(context, operator.left()), value(context, operator.right()), LEFT);
            case NotStartsWith _ ->
                    new SqlNotLike(column(context, operator.left()), value(context, operator.right()), RIGHT);
            case StartsWith _ -> new SqlLike(column(context, operator.left()), value(context, operator.right()), RIGHT);
        };
    }

    /**
     * {@code a.contains(b)} says nothing about which side is the column - a set of values may hold a
     * column, or a column may hold a value - so both are tried.
     *
     * <p>Both, once each. Swapping the operands and starting again reads more neatly and does not
     * end when neither side is a column: it swaps back and forth until the stack runs out, on a
     * query that simply cannot be answered.
     */
    private static SqlSetOperator setOperator(Context context, SetFunction operator) {
        if (isColumn(context, operator.left())) {
            return switch (operator) {
                case InSet inSet -> new SqlInSet(column(context, inSet.left()), value(context, inSet.right()));
                case NotInSet notInSet ->
                        new SqlNotInSet(column(context, notInSet.left()), value(context, notInSet.right()));
            };
        }
        if (isColumn(context, operator.right())) {
            return switch (operator) {
                case InSet inSet -> new SqlInSet(column(context, inSet.right()), value(context, inSet.left()));
                case NotInSet notInSet ->
                        new SqlNotInSet(column(context, notInSet.right()), value(context, notInSet.left()));
            };
        }
        throw new QueryParseException(format(
                "Cannot build a set test from %s: neither side is a column of anything the query reads",
                operator.getClass().getSimpleName()));
    }

    private static SqlLogicalOperator logicalOperator(Context context, LogicalOperator operator) {
        return switch (operator) {
            case And and -> new SqlAnd(expression(context, and.left()), expression(context, and.right()));
            case Or or -> new SqlOr(expression(context, or.left()), expression(context, or.right()));
        };
    }

    private static SqlNull nullOperator(Context context, NullFunction nullFunction) {
        return switch (nullFunction) {
            case IsNotNull isNotNull -> new SqlIsNotNull(column(context, isNotNull.left()));
            case IsNull isNull -> new SqlIsNull(column(context, isNull.left()));
        };
    }

    private static SqlRelationalOperator relationalOperator(Context context, RelationalEvaluation operator) {
        return switch (operator) {
            case Eq(Value left, Value right) -> new SqlEq(column(context, left), value(context, right));
            case Neq(Value left, Value right) -> new SqlNeq(column(context, left), value(context, right));
            case Gt(Value left, Value right) -> new SqlGt(column(context, left), value(context, right));
            case Lt(Value left, Value right) -> new SqlLt(column(context, left), value(context, right));
            case Ge(Value left, Value right) -> new SqlGe(column(context, left), value(context, right));
            case Le(Value left, Value right) -> new SqlLe(column(context, left), value(context, right));
        };
    }
}

