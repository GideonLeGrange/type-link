package me.legrange.typelink.lambda.lookup;

import me.legrange.typelink.lambda.structure.Value;

import java.util.List;
import java.util.function.Function;

public final class VirtualMethod {

    private final Function<List<Value>, Value> eval;

    VirtualMethod(Function<List<Value>, Value> eval) {
        this.eval = eval;
    }

    public Value apply(List<Value> parameters) {
        return eval.apply(parameters);
    }

}
