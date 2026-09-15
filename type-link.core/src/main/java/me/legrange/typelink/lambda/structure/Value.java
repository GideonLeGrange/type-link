package me.legrange.typelink.lambda.structure;

public sealed interface Value permits Argument, Call, Constant, Expression, FieldReference, ListValue, MethodReference, NewObject, Operator, Reference {

    Class<?> type();
}
