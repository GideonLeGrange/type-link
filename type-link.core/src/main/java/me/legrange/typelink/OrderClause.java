package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Lambda;
import me.legrange.typelink.lambda.structure.Value;

import java.util.List;

public final class OrderClause extends Link implements OrderByLink {

    private final boolean reversed;

    public OrderClause(Link left, Value field, boolean reversed) {
        super(left, new Lambda(field, List.of()));
        this.reversed = reversed;
    }

    @Override
    public boolean reversed() {
        return reversed;
    }
}
