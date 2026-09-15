package me.legrange.typelink;

final class Select1<T1, V> extends Selector<SelectFunction1<T1, ?>> implements SelectFunction1<T1, V> {

    Select1(Type type, @SuppressWarnings("unused")  Class<V> resultType, SelectFunction1<T1, ?> function) {
        super(type, function);
    }

    Select1(Type type, SelectFunction1<T1, V> function) {
        super(type, function);
    }

    @Override
    public V apply(T1 t1) {
        return null;
    }
}
