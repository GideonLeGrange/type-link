package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Expression;
import me.legrange.typelink.lambda.structure.Lambda;

import java.util.List;

public final class AndClause extends Link implements AndLink {

    public AndClause(Link left, Expression expression) {
        super(left, new Lambda(expression, List.of()));
    }

}
