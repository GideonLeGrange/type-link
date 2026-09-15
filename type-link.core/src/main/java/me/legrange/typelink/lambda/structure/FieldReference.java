package me.legrange.typelink.lambda.structure;

public sealed interface FieldReference extends Value permits InstanceFieldReference, StaticFieldReference {
}
