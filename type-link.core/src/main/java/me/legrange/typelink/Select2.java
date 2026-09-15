package me.legrange.typelink;

final class Select2<T1, T2, V> extends Selector<SelectFunction2<T1, T2, ?>> implements SelectFunction2<T1, T2, V> {

    Select2(Type type, @SuppressWarnings("unused") Class<V> resultType, SelectFunction2<T1, T2, ?> function) {
        super(type, function);
    }

    Select2(Type type, SelectFunction2<T1, T2, V> function) {
        super(type, function);
    }

    @Override
    public V apply(T1 t1, T2 t2) {
        return null;
    }
}
