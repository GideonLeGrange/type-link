package me.legrange.typelink.lambda.parser;

import me.legrange.typelink.lambda.structure.*;

import static me.legrange.typelink.lambda.parser.Flip.flip;

final class BranchParser {

    static Expression generateExpression(Branch jump) {
        return simplify(expression(jump));
    }

    private static Expression expression(Branch jump) {
        return switch (jump.jump()) {
            case True _ -> switch (jump.cont()) {
                case True _ -> jump.expression();
                case False _ -> flip(jump.expression());
                case Branch contJump -> new Or(jump.expression(), expression(contJump));
            };
            case False _ -> switch (jump.cont()) {
                case True _, False _ -> flip(jump.expression());
                case Branch contJump -> new And(flip(jump.expression()), expression(contJump));
            };
            case Branch jumpJump -> switch (jump.cont()) {
                case True _ -> new Or(flip(jump.expression()), expression(jumpJump));
                case False _ -> new And(jump.expression(), expression(jumpJump));
                case Branch contJump -> choose(jump.expression(), expression(jumpJump), expression(contJump));
            };
        };
    }

    /**
     * Build the expression for a branch where both outcomes lead to further branches: the flow continues as
     * {@code whenTrue} if {@code test} holds, otherwise as {@code whenFalse}.
     * <p>
     * The general answer is {@code (test AND whenTrue) OR (NOT test AND whenFalse)}. That is equivalent to the source
     * when every value is TRUE or FALSE, but not under SQL's three-valued logic: with {@code test} NULL (a LEFT JOIN
     * with no match) it is NULL even when {@code whenTrue} and {@code whenFalse} are both TRUE, so rows are dropped.
     * Short-circuit code puts the same sub-expression behind both outcomes, for example {@code (A && X) || B} jumps
     * to B when A is false and when X is false. When one outcome contains the other, the expression the programmer
     * wrote is rebuilt instead of the expansion:
     * <ul>
     *     <li>{@code whenFalse = X OR N, whenTrue = N}: {@code N OR (NOT test AND X)}</li>
     *     <li>{@code whenFalse = X AND N, whenTrue = N}: {@code N AND (test OR X)}</li>
     *     <li>{@code whenTrue = X OR N, whenFalse = N}: {@code N OR (test AND X)}</li>
     *     <li>{@code whenTrue = X AND N, whenFalse = N}: {@code N AND (NOT test OR X)}</li>
     * </ul>
     */
    private static Expression choose(Expression test, Expression whenTrue, Expression whenFalse) {
        if (whenFalse instanceof Or(var left, var right)) {
            if (left.equals(whenTrue)) {
                return new Or(whenTrue, new And(flip(test), right));
            }
            if (right.equals(whenTrue)) {
                return new Or(whenTrue, new And(flip(test), left));
            }
        }
        if (whenFalse instanceof And(var left, var right)) {
            if (left.equals(whenTrue)) {
                return new And(whenTrue, new Or(test, right));
            }
            if (right.equals(whenTrue)) {
                return new And(whenTrue, new Or(test, left));
            }
        }
        if (whenTrue instanceof Or(var left, var right)) {
            if (left.equals(whenFalse)) {
                return new Or(whenFalse, new And(test, right));
            }
            if (right.equals(whenFalse)) {
                return new Or(whenFalse, new And(test, left));
            }
        }
        if (whenTrue instanceof And(var left, var right)) {
            if (left.equals(whenFalse)) {
                return new And(whenFalse, new Or(flip(test), right));
            }
            if (right.equals(whenFalse)) {
                return new And(whenFalse, new Or(flip(test), left));
            }
        }
        return new Or(new And(test, whenTrue), new And(flip(test), whenFalse));
    }

    private static Expression simplify(Expression expression) {
        return switch (expression) {
            case Evaluation evaluation -> evaluation;
            case LogicalOperator logicalOperator -> logicalOperator(logicalOperator);
            case Not not -> not;
        };
    }

    private static Expression logicalOperator(LogicalOperator op) {
        return switch (op) {
            case And and -> and(and);
            case Or or -> or(or);
        };
    }

    private static Expression and(And and) {
        and = new And(simplify(and.left()), simplify(and.right()));
        if (and.left() instanceof Or(var ll, var lr) && and.right() instanceof Or(var rl, var rr)) {
            if (ll.equals(rl)) {
                return new Or(ll, new And(lr, rr));
            }
            if (lr.equals(rr)) {
                return new Or(new And(ll, rl), lr);
            }
            if (ll.equals(rr)) {
                return new Or(ll, new And(lr, rl));
            }
            if (lr.equals(rl)) {
                return new Or(new And(lr, rr), ll);
            }
        }
        return and;
    }

    private static Expression or(Or or) {
        or = new Or(simplify(or.left()), simplify(or.right()));
        if (or.left() instanceof And(Expression ll, Expression lr) &&
                or.right() instanceof And(Expression rl, Expression rr)) {
            if (ll.equals(rl)) {
                return new And(ll, new Or(lr, rr));
            }
            if (lr.equals(rr)) {
                return new And(lr, new Or(ll, rl));
            }
            if (ll.equals(rr)) {
                return new And(ll, new Or(lr, rl));
            }
            if (lr.equals(rl)) {
                return new And(lr, new Or(ll, rr));
            }
        }
        return or;
    }

    private BranchParser() {
    }

}
