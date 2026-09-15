package me.legrange.typelink.lambda.structure;

import java.lang.reflect.Field;

public record StaticFieldReference(Field field) implements FieldReference {

    public String toString() {
        return field.getName();
    }

    @Override
    public Class<?> type() {
        return field.getType();
    }
}
