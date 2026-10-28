package me.legrange.typelink;

import java.util.List;

final class FromLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements From4<T1, T2, T3, T4>, FromLink {

    private final Database<?> database;
    private final List<Class<?>> types;

    FromLink4(Database<?> database, Class<T1> type1, Class<T2> type2, Class<T3> type3, Class<T4> type4) {
        super(null, null);
        this.database = database;
        types = List.of(type1, type2, type3, type4);
    }

    @Override
    protected Database<?> database() {
        return database;
    }


    @Override
    public List<Class<?>> types() {
        return types;
    }
}
