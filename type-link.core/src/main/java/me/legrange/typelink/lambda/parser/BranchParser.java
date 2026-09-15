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
                case Branch contJump -> new Or(new And(jump.expression(), expression(jumpJump)),
                        new And(flip(jump.expression()), expression(contJump)));
            };
        };
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
