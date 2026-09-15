package me.legrange.typelink;

final class JoinLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements Join3<T1, T2, T3>, JoinLink {

    private final Class<T3> type;
    private final JoinLink.Type joinType;

    JoinLink3(Link left, Class<T3> type, QueryPredicate3<T1, T2, T3> function) {
        this(left, type, Type.INNER, function);
    }

    JoinLink3(Link left, Class<T3> type, JoinLink.Type joinType, QueryPredicate3<T1, T2, T3> function) {
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
