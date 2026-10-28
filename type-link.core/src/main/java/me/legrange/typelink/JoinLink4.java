package me.legrange.typelink;

final class JoinLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements Join4<T1, T2, T3, T4>, JoinLink {

    private final Class<T4> type;
    private final JoinLink.Type joinType;

    JoinLink4(Link left, Class<T4> type, QueryPredicate4<T1, T2, T3, T4> function) {
        this(left, type, Type.INNER, function);
    }

    JoinLink4(Link left, Class<T4> type, JoinLink.Type joinType, QueryPredicate4<T1, T2, T3, T4> function) {
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
