package me.legrange.typelink;

import java.util.List;

final class FromLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements From3<T1, T2, T3>, FromLink {

    private final Database<?> database;
    private final List<Class<?>> types;

    FromLink3(Database<?> database, Class<T1> type1, Class<T2> type2, Class<T3> type3) {
        super(null, null);
        this.database = database;
        types = List.of(type1, type2, type3);
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
