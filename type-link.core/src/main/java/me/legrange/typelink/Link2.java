package me.legrange.typelink;

import me.legrange.typelink.JoinLink.Type;

import java.io.Serializable;
import java.util.List;

abstract sealed class Link2<T1, T2> extends Link permits AndLink2, DistinctLink2, FromLink2, GroupByLink2, HavingLink2, JoinLink2, LimitLink2, OrderByLink2, OrLink2, ThenOrderByLink2, WhereLink2 {

    protected Link2(Link left, Serializable serializable) {
        super(left, serializable);
    }

    public Where2<T1, T2> where(QueryPredicate2<T1, T2> clause) {
        return new WhereLink2<>(this, clause);
    }

    public GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function) {
        return new GroupByLink2<>(this, function);
    }

    public GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function1, SelectFunction2<T1, T2, ?> function2) {
        return new GroupByLink2<>(new GroupByLink2<>(this, function1), function2);
    }

    public GroupBy2<T1, T2> groupBy(SelectFunction2<T1, T2, ?> function1, SelectFunction2<T1, T2, ?> function2, SelectFunction2<T1,T2,?> function3) {
        return new GroupByLink2<>( new GroupByLink2<>(new GroupByLink2<>(this, function1), function2), function3);
    }

    public OrderBy2<T1, T2> orderBy(SelectFunction2<T1, T2, ?> function) {
        return new ThenOrderByLink2<>(this, function, false);
    }

    public OrderBy2<T1, T2> orderByDescending(SelectFunction2<T1, T2, ?> function) {
        return new ThenOrderByLink2<>(this, function, true);
    }

    public <T3> Join3<T1, T2, T3> join(Class<T3> type, QueryPredicate3<T1, T2, T3> join) {
        return new JoinLink3<>(this, type, join);
    }

    public <T3> Join3<T1, T2, T3> leftJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join) {
        return new JoinLink3<>(this, type, Type.LEFT_OUTER, join);
    }

    public <T3> Join3<T1, T2, T3> rightJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join) {
        return new JoinLink3<>(this, type, Type.RIGHT_OUTER, join);
    }

    public <T3> Join3<T1, T2, T3> fullJoin(Class<T3> type, QueryPredicate3<T1, T2, T3> join) {
        return new JoinLink3<>(this, type, Type.FULL_OUTER, join);
    }

    public Limit2<T1, T2> limit(int limit) {
        return new LimitLink2<>(this, limit, 0);
    }

    public Distinct2<T1, T2> distinct() {
        return new DistinctLink2<>(this);
    }

    public Limit2<T1, T2> limit(int limit, int offset) {
        return new LimitLink2<>(this, limit, offset);
    }

    public And2<T1, T2> and(QueryPredicate2<T1, T2> clause) {
        return new AndLink2<>(this, clause);
    }

    public Or2<T1, T2> or(QueryPredicate2<T1, T2> clause) {
        return new OrLink2<>(this, clause);
    }

    public <V1> List<V1> list(SelectFunction2<T1, T2, V1> function1) throws QueryException {
        return database().list(new FunctionSelectionLink(this, function1));
    }

    public List<Row2<T1, T2>> list() throws QueryException{
        return database().list(new FunctionSelectionLink( this, new Select2<>(Selector.Type.LIST_ROW, Row2.class, (T1 _, T2 _) -> null)));
    }
    
    public <V1, V2> List<Row2<V1, V2>> list(SelectFunction2<T1, T2, V1> function1, SelectFunction2<T1, T2, V2> function2) throws QueryException {
        return database().list(new FunctionSelectionLink(new FunctionSelectionLink(this, function1), function2));
    }

    public <V1, V2, V3> List<Row3<V1, V2, V3>> list(SelectFunction2<T1, T2, V1> function1, SelectFunction2<T1, T2, V2> function2, SelectFunction2<T1, T2, V3> function3) throws QueryException {
        return database().list(new FunctionSelectionLink(new FunctionSelectionLink(new FunctionSelectionLink(this, function1), function2), function3));
    }

    public <N1 extends Number> N1 sum(SelectFunction2<T1, T2, N1> function1) {
        return aggregate(Selects.sum(function1));
    }

    public <N1 extends Number> N1 min(SelectFunction2<T1, T2, N1> function1) {
        return aggregate(Selects.min(function1));
    }

    public <N1 extends Number> N1 max(SelectFunction2<T1, T2, N1> function1) {
        return aggregate(Selects.max(function1));
    }

    public Long count(SelectFunction2<T1, T2, ?> function1) {
        return aggregate(Selects.count(function1));
    }

    public Long count() {
        return count(null);
    }

    public Having2<T1, T2> having(QueryPredicate2<T1, T2> predicate) {
        return new HavingLink2<>(this, predicate);
    }

    @SuppressWarnings("unused")
    public <N1 extends Number> Double avg(SelectFunction2<T1, T2, Double> function1) {
        return aggregate(Selects.avg(function1));
    }

    public OrderBy2<T1, T2> thenBy(SelectFunction2<T1, T2, ?> function) {
        return new ThenOrderByLink2<>(this, function, false);
    }

    public OrderBy2<T1, T2> thenByDescending(SelectFunction2<T1, T2, ?> function) {
        return new ThenOrderByLink2<>(this, function, true);
    }

    private <V1> V1 aggregate(SelectFunction2<T1, T2, V1> function1) {
        return (V1) database().object(append(function1, List.of()));
    }

    public <V1, V2> Row2<V1, V2> aggregate(SelectFunction2<T1, T2, V1> function1, SelectFunction2<T1, T2, V2> function2) {
        return (Row2<V1, V2>) database().object(append(function1, List.of(function2)));
    }

    public <V1, V2, V3> Row3<V1, V2, V3> aggregate(SelectFunction2<T1, T2, V1> function1, SelectFunction2<T1, T2, V2> function2, SelectFunction2<T1, T2, V3> function3) {
        return (Row3<V1, V2, V3>) database().object(append(function1, List.of(function2, function3)));
    }

    private <L extends Link & Selection> L append(SelectFunction2<T1, T2, ?> first, List<SelectFunction2<T1, T2, ?>> rest) {
        var select = new FunctionSelectionLink(this, first);
        for (var function : rest) {
            select = new FunctionSelectionLink(select, function);
        }
        //noinspection unchecked
        return (L) select;
    }

}
