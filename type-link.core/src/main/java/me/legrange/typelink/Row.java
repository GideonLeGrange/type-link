package me.legrange.typelink;

public sealed interface Row permits Row1, Row2, Row3, Row4 {

    Object get(int column);

    Object get(SelectFunction function);

    int columCount();
}
