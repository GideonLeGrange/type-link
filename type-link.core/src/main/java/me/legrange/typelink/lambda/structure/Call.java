package me.legrange.typelink.lambda.structure;

public sealed interface Call extends Value permits ConstructorCall, MethodCall, StaticMethodCall {
}
