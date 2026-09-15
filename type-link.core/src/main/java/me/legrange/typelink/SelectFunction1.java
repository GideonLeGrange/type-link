package me.legrange.typelink;

import java.io.Serializable;

@FunctionalInterface
public non-sealed interface SelectFunction1<T1, T> extends Serializable, SelectFunction {

    T apply(T1 t1);

}
