package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Expression;
import me.legrange.typelink.lambda.structure.Lambda;

import java.util.List;

public final class HavingClause extends Link implements HavingLink {

    public HavingClause(Link left, Expression expression) {
        super(left, new Lambda(expression, List.of()));
    }

}
