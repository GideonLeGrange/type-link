package me.legrange.typelink;

public final class FunctionSelectionLink extends SelectionLink {

    FunctionSelectionLink(Link left, SelectFunction function) {
        super(left, function);
    }

    public SelectFunction getFunction() {
        return (SelectFunction) function();
    }

}
