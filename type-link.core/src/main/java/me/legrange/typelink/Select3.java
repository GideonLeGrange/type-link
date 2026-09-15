package me.legrange.typelink;

final class Select3<T1, T2, T3, V> extends Selector<SelectFunction3<T1, T2, T3, ?>> implements SelectFunction3<T1, T2, T3, V> {

    Select3(Type type, @SuppressWarnings("unused") Class<V> resultType, SelectFunction3<T1, T2, T3, ?> function) {
        super(type, function);
    }

    Select3(Type type, SelectFunction3<T1, T2, T3, V> function) {
        super(type, function);
    }

    @Override
    public V apply(T1 t1, T2 t2, T3 t3) {
        return null;
    }
}
