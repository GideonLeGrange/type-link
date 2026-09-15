package me.legrange.typelink;

public abstract sealed class Selector<F extends SelectFunction> permits Select1, Select2, Select3 {

    public enum Type {
        MIN_COLUMN, MAX_COLUMN, SUM_COLUMN, AVG_COLUMN, COUNT_COLUMN, COUNT_ROWS, LIST_ROW
    }

    private final Type type;
    private final F function;

    protected Selector(Type type, F function) {
        this.type = type;
        this.function = function;
    }

    public SelectFunction getFunction() {
        return function;
    }

    public Type type() {
        return type;
    }
}
