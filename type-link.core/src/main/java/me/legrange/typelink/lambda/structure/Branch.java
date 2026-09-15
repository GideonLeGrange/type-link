package me.legrange.typelink.lambda.structure;

public record Branch(Expression expression, Flow cont, Flow jump) implements ByteCodeModel, Flow {
}
