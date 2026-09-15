package me.legrange.typelink.lambda.parser;

import me.legrange.typelink.lambda.structure.*;

final class Flip {

    private Flip() {
    }

    static Expression flip(Expression evaluation) {
        return switch (evaluation) {
            case DateFunction fo -> flip(fo);
            case NullFunction no -> flip(no);
            case RelationalEvaluation ro -> flip(ro);
            case SetFunction so -> flip(so);
            case WildcardFunction wo -> flip(wo);
            case LogicalOperator lo -> flip(lo);
            case Not not -> new Not(flip(not.expression()));
        };
    }

    private static LogicalOperator flip(LogicalOperator lo) {
        return switch (lo) {
            case And(var left, var right) -> new Or(flip(left), flip(right));
            case Or(var left, var right) -> new And(flip(left), flip(right));  // Apply De Morgan's law
        };
    }

    private static DateFunction flip(DateFunction fo) {
        return switch (fo) {
            case IsAfter isAfter -> isAfter;
            case IsBefore isBefore -> isBefore;
        };
    }

    private static NullFunction flip(NullFunction no) {
        return switch (no) {
            case IsNotNull(Value value) -> new IsNull(value);
            case IsNull(Value value) -> new IsNotNull(value);
        };
    }

    private static RelationalEvaluation flip(RelationalEvaluation ro) {
        return switch (ro) {
            case Eq(Value left, Value right) -> new Neq(left, right);
            case Ge(Value left, Value right) -> new Lt(left, right);
            case Gt(Value left, Value right) -> new Le(left, right);
            case Le(Value left, Value right) -> new Gt(left, right);
            case Lt(Value left, Value right) -> new Ge(left, right);
            case Neq(Value left, Value right) -> new Eq(left, right);
        };
    }

    private static SetFunction flip(SetFunction so) {
        return switch (so) {
            case InSet(Value left, Value right) -> new NotInSet(left, right);
            case NotInSet(Value left, Value right) -> new InSet(left, right);
        };
    }

    private static WildcardFunction flip(WildcardFunction wo) {
        return switch (wo) {
            case Contains(Value left, Value right) -> new NotContains(left, right);
            case EndsWith(Value left, Value right) -> new NotEndsWith(left, right);
            case NotContains(Value left, Value right) -> new Contains(left, right);
            case NotEndsWith(Value left, Value right) -> new EndsWith(left, right);
            case NotStartsWith(Value left, Value right) -> new StartsWith(left, right);
            case StartsWith(Value left, Value right) -> new NotStartsWith(left, right);
        };
    }

}
