package me.legrange.typelink;

import java.io.Serializable;

@FunctionalInterface
public non-sealed interface SelectFunction3<T1, T2, T3, T> extends Serializable, SelectFunction {

    T apply(T1 t1, T2 t2, T3 t3);

}
