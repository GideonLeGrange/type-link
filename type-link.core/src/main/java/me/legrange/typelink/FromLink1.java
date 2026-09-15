package me.legrange.typelink;

import java.util.List;

final class FromLink1<T1> extends Link1<T1> implements From1<T1>, FromLink {

    private final Database<?> database;
    private final Class<T1> type;

    FromLink1(Database<?> database, Class<T1> type) {
        super(null, null);
        this.type = type;
        this.database = database;
    }

    @Override
    public List<Class<?>> types() {
        return List.of(type);
    }

    @Override
    protected Database<?> database() {
        return database;
    }

}
