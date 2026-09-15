package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Lambda;

import java.io.Serializable;

public abstract sealed class SelectionLink extends Link implements Selection permits FunctionSelectionLink, ValueSelectionLink {
    protected SelectionLink(Link left, Serializable function) {
        super(left, function);
    }

    protected SelectionLink(Link left, Lambda lambda) {
        super(left, lambda);
    }


}
