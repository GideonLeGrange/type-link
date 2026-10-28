package me.legrange.typelink;

final class Select4<T1, T2, T3, T4, V> extends Selector<SelectFunction4<T1, T2, T3, T4, ?>> implements SelectFunction4<T1, T2, T3, T4, V> {

    Select4(Type type, @SuppressWarnings("unused") Class<V> resultType, SelectFunction4<T1, T2, T3, T4, ?> function) {
        super(type, function);
    }

    Select4(Type type, SelectFunction4<T1, T2, T3, T4, V> function) {
        super(type, function);
    }

    @Override
    public V apply(T1 t1, T2 t2, T3 t3, T4 t4) {
        return null;
    }
}
