package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Expression;
import me.legrange.typelink.lambda.structure.Lambda;

import java.util.List;

public final class WhereClause extends Link implements WhereLink {

    public WhereClause(Link left, Expression expression) {
        super(left, new Lambda(expression, List.of()));
    }

}
