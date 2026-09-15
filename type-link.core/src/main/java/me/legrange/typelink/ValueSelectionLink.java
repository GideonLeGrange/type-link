package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Lambda;
import me.legrange.typelink.lambda.structure.Value;

import java.util.List;

public final class ValueSelectionLink extends SelectionLink {

    public ValueSelectionLink(Link left, Value selection) {
        super(left, new Lambda(selection, List.of()));
    }

}
