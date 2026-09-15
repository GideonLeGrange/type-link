package me.legrange.typelink.sql.parser;

import me.legrange.typelink.*;
import me.legrange.typelink.lambda.parser.LambdaParser;
import me.legrange.typelink.lambda.structure.*;
import me.legrange.typelink.sql.structure.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static me.legrange.typelink.sql.parser.QueryParser.parseChain;
import static me.legrange.typelink.sql.parser.SubQueryParser.buildChain;
import static me.legrange.typelink.sql.parser.Values.fieldValue;

/**
 * Resolves {@link Value} trees into {@link SqlColumn} nodes.
 * Determines whether a value represents a database column and maps it
 * to the corresponding SQL column, table, or aggregate function.
 */
@SuppressWarnings("unchecked")
final class ColumnResolver {

    private ColumnResolver() {
    }

    static SqlColumn columns(Context context, List<Link> link) {
        var list = link.stream()
                .filter(l -> l instanceof SelectionLink)
                .map(SelectionLink.class::cast)
                .toList();
        if (list.isEmpty()) {
            throw new QueryParseException("No SELECT in query. BUG!");
        }
        var res = new ArrayList<SqlColumn>();
        for (var select : list) {
            if (select instanceof FunctionSelectionLink fsl && fsl.getFunction() instanceof Selector<?> selector) {
                res.addAll(columns(context, select, selector));
            } else {
                res.addAll(columns(context, select.lambda()));
            }
        }
        return switch (res.size()) {
            case 0 -> throw new QueryParseException("No columns in query. BUG!");
            case 1 -> res.getFirst();
            default -> new SqlAll(res);
        };
    }

    static SqlColumn column(Context context, Value value) {
        return switch (value) {
            case Constant<?> cv -> constant(cv);
            case StaticFieldReference fw -> constant(fw);
            case Argument arg -> tableForType(context, arg.type());
            case ListValue _ -> throw new QueryParseException("List value not supported. BUG!");
            case MethodCall methodCall -> methodCall(context, methodCall);
            case ConstructorCall constructorCall -> constructorCall(context, constructorCall);
            case Operator operator -> operator(context, operator);
            case MethodReference _ -> throw new QueryParseException("Method reference not supported. BUG!");
            case StaticMethodCall _ -> throw new QueryParseException("Static method calls not supported. BUG!");
            case InstanceFieldReference fi -> columnForField(context, fi);
            case Expression _ -> throw new QueryParseException("Expression not supported. BUG!");
            case NewObject no -> columnForNewObject(context, no);
            case Reference _ -> throw new QueryParseException("Reference not supported. BUG!");
        };
    }

    static boolean isColumn(Context context, Value value) {
        return switch (value) {
            case MethodCall mc -> isColumn(context, mc);
            case Argument arg -> arg.isRowParameter() && context.mapper().isTable(tableFor(context, arg));
            case StaticFieldReference _, Constant<?> _, ListValue _, ConstructorCall _, Operator _ -> false;
            case InstanceFieldReference f -> context.mapper().isColumn(tableFor(context, f.target()), f.field());
            case MethodReference ref -> context.mapper().isColumn(ref.method());
            case StaticMethodCall _, Expression _, NewObject _, Reference _ -> false;
        };
    }

    static SqlColumn column(Context context, Lambda lambda) {
        var select = columns(context, lambda);
        if (select.size() == 1) {
            return select.getFirst();
        }
        throw new QueryParseException("Expected one column, but got " + select.size() + " columns");
    }

    private static SqlColumn column(Context context, Serializable function) {
        return column(context, LambdaParser.parse(function));
    }

    private static List<SqlColumn> columns(Context context, Lambda lambda) {
        return switch (lambda.value()) {
            case ConstructorCall cc -> constructorCallList(context, cc);
            case MethodReference _ -> throw new QueryParseException("Method reference not supported in lambda. BUG!");
            case NewObject newObject -> newObject.fields().stream().map(field -> column(context, field)).toList();
            default -> List.of(column(context, lambda.value()));
        };
    }

    private static SqlColumn methodCall(Context context, MethodCall mc) {
        var target = mc.target();
        if (Clause.class.isAssignableFrom(target.type()) || Database.class.isAssignableFrom(target.type())) {
            return new SqlSubSelect(parseChain(buildChain(mc), context));
        }
        var method = mc.method();
        var type = tableFor(context, target);
        if (context.mapper().isColumn(type, method)) {
            // The type as well as the method: one accessor inherited from a base class can mean a
            // different column in every table, and only the type says which.
            return new SqlTableColumn(context.mapper().tableName(type),
                    context.mapper().columnName(type, method), method.getReturnType());
        }
        throw new QueryParseException(format("Don't know how to determine SQL column from %s. BUG!", method.getName()));
    }

    private static SqlColumn constructorCall(Context context, ConstructorCall cc) {
        var list = constructorCallList(context, cc);
        if (list.size() == 1) {
            return list.getFirst();
        }
        return new SqlAll(list);
    }

    private static List<SqlColumn> constructorCallList(Context context, ConstructorCall cc) {
        return cc.parameters().stream().map(val -> column(context, val)).toList();
    }

    private static List<SqlColumn> columns(Context context, Link link, Selector<?> selector) {
        return switch (selector.type()) {
            case COUNT_COLUMN -> countColumnSelect(context, link, selector.getFunction());
            case SUM_COLUMN -> List.of(new SqlSum(column(context, selector.getFunction())));
            case AVG_COLUMN -> List.of(new SqlAvg(column(context, selector.getFunction())));
            case MIN_COLUMN -> List.of(new SqlMin(column(context, selector.getFunction())));
            case MAX_COLUMN -> List.of(new SqlMax(column(context, selector.getFunction())));
            case COUNT_ROWS -> rowsSelect(context, link);
            case LIST_ROW -> List.of(new SqlAll(types(link).stream()
                    .map(type -> new SqlTable(tableRef(context, type), tableColumns(context, type)))
                    .map(SqlColumn.class::cast)
                    .toList()));
        };
    }

    private static List<SqlColumn> countColumnSelect(Context context, Link link, SelectFunction function) {
        return List.of(new SqlCount(function == null ? new SqlAll(types(link).stream()
                .map(type -> new SqlTable(tableRef(context, type), tableColumns(context, type)))
                .map(SqlColumn.class::cast)
                .toList()) : column(context, function)));
    }

    /** Resolve a type to the table name the mapper gives it.
     * Which table a column read off {@code value} belongs to.
     *
     * <p>One of the lambda's own parameters stands for a table of the query it appears in, and its
     * position says which - the fluent API pairs them in order. Anything else, including a row
     * captured from an enclosing scope, keeps its declared type; an enclosing row's table is not
     * this scope's to decide.
     *
     * <p>Both halves used to be guessed, differently. Taking the declared type of a parameter is
     * wrong as soon as the lambda is declared over a supertype, because it names a table the query
     * does not contain. Taking "the single table in context" is wrong inside a correlated
     * sub-select, because an enclosing row's columns do not belong to the inner scope's table.
     */
    private static Class<?> tableFor(Context context, Value value) {
        if (value instanceof Argument arg && arg.isRowParameter()) {
            // A row's position counts the tables of the scope that declared it, which for a
            // correlated reference is not the scope doing the referring.
            var scope = context.at(arg.scopeDepth());
            if (scope != null && arg.rowPosition() < scope.types().size()) {
                return scope.types().get(arg.rowPosition());
            }
        }
        return value.type();
    }

    static SqlTableRef tableRef(Context context, Class<?> type) {
        return new SqlTableRef(type, context.mapper().tableName(type));
    }

    static List<SqlTableColumn> tableColumns(Context context, Class<?> type) {
        var tableName = context.mapper().tableName(type);
        List<String> columnNames = context.mapper().columnNames(type);
        return columnNames.stream()
                .map(name -> new SqlTableColumn(tableName, name, context.mapper().columnType(type, name)))
                .toList();
    }

    private static List<SqlColumn> rowsSelect(Context context, Link link) {
        return List.of(new SqlCount(new SqlAll(types(link).stream()
                .flatMap(type -> tableColumns(context, type).stream())
                .map(SqlColumn.class::cast)
                .toList())));
    }

    private static List<Class<?>> types(Link link) {
        var res = new ArrayList<Class<?>>();
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

    private static boolean isColumn(Context context, MethodCall methodCall) {
        var target = methodCall.target();
        if (target instanceof MethodCall mc) {
            return isColumn(context, mc);
        }
        if (target instanceof Argument arg && !arg.isRowParameter()) {
            // Read on something the lambda captured rather than on a row of any scope here. Its
            // members are values to bind, whatever its type happens to look like - and an erased row
            // looks exactly like the base type an ORM maps.
            return false;
        }
        // The table it is called on, as well as the method - the same pair {@link #methodCall} uses
        // to name the column. Asking about the method alone answers no whenever the accessor was
        // declared somewhere that is not a table, and then a plain column read is taken for
        // something else entirely.
        return context.mapper().isColumn(tableFor(context, target), methodCall.method());
    }

    private static SqlColumn columnForNewObject(Context context, NewObject no) {
        return new SqlAll(no.fields().stream()
                .map(field -> column(context, field))
                .toList());
    }

    private static SqlColumn constant(Constant<?> constant) {
        return new SqlConstant(constant.value());
    }

    private static SqlColumn constant(StaticFieldReference fv) {
        return new SqlConstant(fieldValue(fv));
    }

    static SqlColumn operator(Context context, Operator operator) {
        return switch (operator) {
            case BinaryOperator binaryOperator -> binaryOperator(context, binaryOperator);
            case AggregationOperator aggregationOperator -> aggregationOperator(context, aggregationOperator);
            case FunctionOperator functionOperator -> functionOperator(context, functionOperator);
        };
    }

    private static SqlOperation binaryOperator(Context context, BinaryOperator operator) {
        return switch (operator) {
            case Add(_, Value left, Value right) -> new SqlAdd(column(context, left), column(context, right));
            case Subtract(_, Value left, Value right) -> new SqlSubtract(column(context, left), column(context, right));
            case Multiply(_, Value left, Value right) -> new SqlMultiply(column(context, left), column(context, right));
            case Divide(_, Value left, Value right) -> new SqlDivide(column(context, left), column(context, right));
        };
    }

    private static SqlFunction aggregationOperator(Context context, AggregationOperator op) {
        return switch (op) {
            case Count(Value left) -> new SqlCount(left == null ? new SqlAll(List.of()) : column(context, left));
            case Sum sum -> new SqlSum(column(context, sum.left()));
            case Avg avg -> new SqlAvg(column(context, avg.left()));
            case Min(Value left) -> new SqlMin(column(context, left));
            case Max(Value left) -> new SqlMax(column(context, left));
        };
    }
    
    private static SqlColumn functionOperator(Context context, FunctionOperator op) {
        return switch (op) {
            case Concat concat -> new SqlConcat(concat.values().stream().map(value -> column(context, value)).toList());
        };
    }

    private static SqlColumn columnForField(Context context, InstanceFieldReference fi) {
        var type = tableFor(context, fi.target());
        if (context.mapper().isColumn(type, fi.field())) {
            return new SqlTableColumn(context.mapper().tableName(type),
                    context.mapper().columnName(type, fi.field()),
                    fi.field().getType());
        }
        throw new QueryParseException(format("Don't know how to determine SQL column from %s.%s. BUG!", fi.field().getDeclaringClass().getSimpleName(), fi.field().getName()));
    }

    private static SqlTable tableForType(Context context, Class<?> type) throws QueryParseException {
        if (context.mapper().isTable(type)) {
            return new SqlTable(tableRef(context, type), tableColumns(context, type));
        }
        throw new QueryParseException(format("Don't know how to determine SQL table from %s. BUG!", type.getSimpleName()));
    }
}

