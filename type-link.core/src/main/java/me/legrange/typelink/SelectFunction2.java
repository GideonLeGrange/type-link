package me.legrange.typelink;

import java.io.Serializable;

@FunctionalInterface
public non-sealed interface SelectFunction2<T1, T2, T> extends Serializable, SelectFunction {

    T apply(T1 t1, T2 t2);

}
