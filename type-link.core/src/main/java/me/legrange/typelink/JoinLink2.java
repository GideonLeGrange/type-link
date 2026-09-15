package me.legrange.typelink;

final class JoinLink2<T1, T2> extends Link2<T1, T2> implements Join2<T1, T2>, JoinLink {

    private final Class<T2> type;
    private final Type joinType;

    JoinLink2(Link left, Class<T2> type, QueryPredicate2<T1, T2> function) {
        this(left, type, Type.INNER, function);
    }

    JoinLink2(Link left, Class<T2> type, Type joinType, QueryPredicate2<T1, T2> function) {
        super(left, function);
        this.type = type;
        this.joinType = joinType;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Type joinType() {
        return joinType;
    }

}
