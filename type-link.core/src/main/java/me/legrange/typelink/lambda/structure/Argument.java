package me.legrange.typelink.lambda.structure;

/**
 * An argument of the lambda body being decoded.
 *
 * @param slot        the JVM local variable slot. {@code long} and {@code double} occupy two slots
 *                    each, so slot numbers run ahead of argument positions.
 * @param index       the argument's position in the method's parameter list, counting {@code this}
 *                    as 0 on an instance method. For a captured argument this is also its position
 *                    in the lambda's captured argument list.
 * @param rowPosition for a row parameter, its position among the parameters of the lambda that
 *                    declared it - which is also the position of the table it refers to in that
 *                    scope's FROM. Negative for a captured value, which is not a row at all.
 * @param scopeDepth  how many scopes out the row belongs to: zero for this lambda's own parameter,
 *                    one for the row of the lambda around it, and so on. A correlated sub-select
 *                    refers to the row outside it, and that row's position means nothing in the
 *                    tables of the scope doing the referring. Meaningless when {@code rowPosition}
 *                    is negative.
 * @param name        the argument's name where debug information supplies one, else synthesised.
 * @param type        the argument's declared type.
 */
public record Argument(int slot, int index, int rowPosition, int scopeDepth, String name, Class<?> type) implements Value {

    /** Whether this is one of the lambda's own parameters rather than something it captured. */
    public boolean isRowParameter() {
        return rowPosition >= 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
