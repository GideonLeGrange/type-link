package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Lambda;
import me.legrange.typelink.lambda.structure.Value;

import java.util.List;

public final class GroupClause extends Link implements GroupByLink {

    public GroupClause(Link left, Value clause) {
        super(left, new Lambda(clause, List.of()));
    }

}
