package me.legrange.typelink;

import java.util.List;

final class FromLink2<T1, T2> extends Link2<T1, T2> implements From2<T1, T2>, FromLink {

    private final Database<?> database;
    private final List<Class<?>> types;

    FromLink2(Database<?> database, Class<T1> type1, Class<T2> type2) {
        super(null, null);
        this.database = database;
        types = List.of(type1, type2);
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
