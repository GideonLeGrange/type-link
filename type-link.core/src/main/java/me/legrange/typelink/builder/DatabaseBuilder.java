package me.legrange.typelink.builder;

public final class DatabaseBuilder<O> {

    public static <O> WithType<O> of(Class<O> type) {
        return new WithType<>(type);
    }

    private DatabaseBuilder() {
    }

}
