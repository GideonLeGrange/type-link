package me.legrange.typelink.lambda.parser;

import me.legrange.typelink.lambda.structure.*;

import java.util.List;
import java.util.function.Function;

final class Dereference {

    private Dereference() {
    }

    static Value dereference(Value value, Function<Reference, Value> function) {
        return switch (value) {
            case null -> null;
            case Call call -> switch (call) {
                case ConstructorCall cc -> new ConstructorCall(cc.type(), cc.constructor(), dereference(cc.parameters(), function));
                case MethodCall(var target, var method, var params) ->
                        new MethodCall(dereference(target, function), method, params.stream().map(par -> dereference(par, function)).toList());
                case StaticMethodCall(var type, var parameters) ->
                        new StaticMethodCall(type, parameters.stream().map(val -> dereference(val, function)).toList());

            };
            case Constant<?> constant -> constant;
            case Expression expression -> dereference(expression, function);
            case InstanceFieldReference(var target, var field) when target instanceof Reference ref ->
                    new InstanceFieldReference(dereference(ref, function), field);
            case InstanceFieldReference field -> field;
            case ListValue(var values) ->
                    new ListValue(values.stream().map(val -> dereference(val, function)).toList());
            case MethodReference mr -> mr;
            case NewObject(var type, var fields) ->
                    new NewObject(type, fields.stream().map(field -> dereference(field, function)).toList());
            case Operator operator -> dereference(operator, function);
            case Reference reference -> {
                var object = function.apply(reference);
                yield dereference(object, function);
            }
            case StaticFieldReference staticField -> staticField;
            case Argument argument -> argument;
        };
    }

    static Expression dereference(Expression evaluation, Function<Reference, Value> function) {
        return switch (evaluation) {
            case DateFunction fo -> dereference(fo, function);
            case NullFunction no -> dereference(no, function);
            case RelationalEvaluation ro -> dereference(ro, function);
            case SetFunction so -> dereference(so, function);
            case WildcardFunction wo -> dereference(wo, function);
            case LogicalOperator lo -> dereference(lo, function);
            case Not not -> new Not(dereference(not.expression(), function));
        };
    }

    private static List<Value> dereference(List<Value> list, Function<Reference, Value> function) {
        return list.stream().map(val -> dereference(val, function)).toList();
    }


    private static Operator dereference(Operator operator, Function<Reference, Value> function) {
        return switch (operator) {
            case AggregationOperator ao -> dereference(ao, function);
            case BinaryOperator bo -> dereference(bo, function);
            case FunctionOperator fo -> dereference(fo, function);
        };
    }

    private static AggregationOperator dereference(AggregationOperator ao, Function<Reference, Value> function) {
        return switch (ao) {
            case Avg(var value) -> new Avg(dereference(value, function));
            case Count(var value) -> new Count(dereference(value, function));
            case Max(var value) -> new Max(dereference(value, function));
            case Min(var value) -> new Min(dereference(value, function));
            case Sum(var value) -> new Sum(dereference(value, function));
        };
    }

    private static FunctionOperator dereference(FunctionOperator fo, Function<Reference, Value> function) {
        return switch (fo) {
            case Concat(var values) -> new Concat(dereference(values, function));
        };
    }

    private static BinaryOperator dereference(BinaryOperator bo, Function<Reference, Value> function) {
        return switch (bo) {
            case Add(var type, var left, var right) ->
                    new Add(type, dereference(left, function), dereference(right, function));
            case Divide(var type, var left, var right) ->
                    new Divide(type, dereference(left, function), dereference(right, function));
            case Multiply(var type, var left, var right) ->
                    new Multiply(type, dereference(left, function), dereference(right, function));
            case Subtract(var type, var left, var right) ->
                    new Subtract(type, dereference(left, function), dereference(right, function));
        };
    }

    private static Expression dereference(DateFunction fo, Function<Reference, Value> function) {
        return switch (fo) {
            case IsAfter(var left, var right) -> new IsAfter(dereference(left, function), dereference(right, function));
            case IsBefore(var left, var right) ->
                    new IsBefore(dereference(left, function), dereference(right, function));
        };
    }

    private static Expression dereference(NullFunction no, Function<Reference, Value> function) {
        return switch (no) {
            case IsNotNull(var value) -> new IsNotNull(dereference(value, function));
            case IsNull(var value) -> new IsNull(dereference(value, function));
        };
    }

    private static Expression dereference(WildcardFunction wo, Function<Reference, Value> function) {
        return switch (wo) {
            case Contains(var left, var right) ->
                    new Contains(dereference(left, function), dereference(right, function));
            case EndsWith(var left, var right) ->
                    new EndsWith(dereference(left, function), dereference(right, function));
            case NotContains(var left, var right) ->
                    new NotContains(dereference(left, function), dereference(right, function));
            case NotEndsWith(var left, var right) ->
                    new NotEndsWith(dereference(left, function), dereference(right, function));
            case NotStartsWith(var left, var right) ->
                    new NotStartsWith(dereference(left, function), dereference(right, function));
            case StartsWith(var left, var right) ->
                    new StartsWith(dereference(left, function), dereference(right, function));
        };
    }

    private static Expression dereference(SetFunction so, Function<Reference, Value> function) {
        return switch (so) {
            case InSet(var left, var right) -> new InSet(dereference(left, function), dereference(right, function));
            case NotInSet(var left, var right) ->
                    new NotInSet(dereference(left, function), dereference(right, function));
        };
    }

    private static Expression dereference(RelationalEvaluation ro, Function<Reference, Value> function) {
        return switch (ro) {
            case Eq(var left, var right) -> new Eq(dereference(left, function), dereference(right, function));
            case Ge(var left, var right) -> new Ge(dereference(left, function), dereference(right, function));
            case Gt(var left, var right) -> new Gt(dereference(left, function), dereference(right, function));
            case Le(var left, var right) -> new Le(dereference(left, function), dereference(right, function));
            case Lt(var left, var right) -> new Lt(dereference(left, function), dereference(right, function));
            case Neq(var left, var right) -> new Neq(dereference(left, function), dereference(right, function));
        };
    }

    private static Expression dereference(LogicalOperator lo, Function<Reference, Value> function) {
        return switch (lo) {
            case And(var left, var right) -> new And(dereference(left, function), dereference(right, function));
            case Or(var left, var right) -> new Or(dereference(left, function), dereference(right, function));
        };
    }
}
