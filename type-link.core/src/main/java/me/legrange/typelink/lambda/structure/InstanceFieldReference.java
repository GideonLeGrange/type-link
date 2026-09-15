package me.legrange.typelink.lambda.structure;

import java.lang.reflect.Field;

public record InstanceFieldReference(Value target, Field field) implements  FieldReference {
    @Override
    public Class<?> type() {
        return field.getType();
    }
}
