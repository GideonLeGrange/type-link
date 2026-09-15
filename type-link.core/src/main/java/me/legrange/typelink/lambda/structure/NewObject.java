package me.legrange.typelink.lambda.structure;

import java.util.List;

public record NewObject(Class<?> type, List<Value> fields) implements Value {
}
