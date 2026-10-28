package me.legrange.typelink;

import java.io.Serializable;
import java.util.List;

abstract sealed class Link4<T1, T2, T3, T4> extends Link permits AndLink4, DistinctLink4, FromLink4, GroupByLink4, HavingLink4, JoinLink4, LimitLink4, OrderByLink4, OrLink4, ThenOrderByLink4, WhereLink4 {

    protected Link4(Link left, Serializable serializable) {
        super(left, serializable);
    }

    public Where4<T1, T2, T3, T4> where(QueryPredicate4<T1, T2, T3, T4> clause) {
        return new WhereLink4<>(this, clause);
    }

    public GroupBy4<T1, T2, T3, T4> groupBy(SelectFunction4<T1, T2, T3, T4, ?> function) {
        return new GroupByLink4<>(this, function);
    }

    public GroupBy4<T1, T2, T3, T4> groupBy(SelectFunction4<T1, T2, T3, T4, ?> function1, SelectFunction4<T1, T2, T3, T4, ?> function2) {
        return new GroupByLink4<>(new GroupByLink4<>(this, function1), function2);
    }

    public GroupBy4<T1, T2, T3, T4> groupBy(SelectFunction4<T1, T2, T3, T4, ?> function1, SelectFunction4<T1, T2, T3, T4, ?> function2, SelectFunction4<T1, T2, T3, T4, ?> function3) {
        return new GroupByLink4<>(new GroupByLink4<>(new GroupByLink4<>(this, function1), function2), function3);
    }

    public OrderBy4<T1, T2, T3, T4> orderBy(SelectFunction4<T1, T2, T3, T4, ?> function) {
        return new ThenOrderByLink4<>(this, function, false);
    }

    public Limit4<T1, T2, T3, T4> limit(int limit) {
        return limit(limit, 0);
    }

    public Distinct4<T1, T2, T3, T4> distinct() {
        return new DistinctLink4<>(this);
    }

    public Limit4<T1, T2, T3, T4> limit(int limit, int offset) {
        return new LimitLink4<>(this, limit, offset);
    }

    public And4<T1, T2, T3, T4> and(QueryPredicate4<T1, T2, T3, T4> clause) {
        return new AndLink4<>(this, clause);
    }

    public Or4<T1, T2, T3, T4> or(QueryPredicate4<T1, T2, T3, T4> clause) {
        return new OrLink4<>(this, clause);
    }

    public OrderBy4<T1, T2, T3, T4> orderByDescending(SelectFunction4<T1, T2, T3, T4, ?> function) {
        return new ThenOrderByLink4<>(this, function, true);
    }

    public <V1> List<V1> list(SelectFunction4<T1, T2, T3, T4, V1> function1) {
        return database().list(new FunctionSelectionLink(this, function1));
    }

    public List<Row4<T1, T2, T3, T4>> list() {
        return database().list(new FunctionSelectionLink(this, new Select4<>(Selector.Type.LIST_ROW, Row4.class, (T1 _, T2 _, T3 _, T4 _) -> null)));
    }

    public <V1, V2> List<Row2<V1, V2>> list(SelectFunction4<T1, T2, T3, T4, V1> function1, SelectFunction4<T1, T2, T3, T4, V2> function2) {
        return database().list(new FunctionSelectionLink(new FunctionSelectionLink(this, function1), function2));
    }

    public <V1, V2, V3> List<Row3<V1, V2, V3>> list(SelectFunction4<T1, T2, T3, T4, V1> function1, SelectFunction4<T1, T2, T3, T4, V2> function2, SelectFunction4<T1, T2, T3, T4, V3> function3) {
        return database().list(new FunctionSelectionLink(new FunctionSelectionLink(new FunctionSelectionLink(this, function1), function2), function3));
    }

    public <V1, V2, V3, V4> List<Row4<V1, V2, V3, V4>> list(SelectFunction4<T1, T2, T3, T4, V1> function1, SelectFunction4<T1, T2, T3, T4, V2> function2, SelectFunction4<T1, T2, T3, T4, V3> function3, SelectFunction4<T1, T2, T3, T4, V4> function4) {
        return database().list(new FunctionSelectionLink(new FunctionSelectionLink(new FunctionSelectionLink(new FunctionSelectionLink(this, function1), function2), function3), function4));
    }

    public OrderBy4<T1, T2, T3, T4> thenBy(SelectFunction4<T1, T2, T3, T4, ?> function) {
        return new ThenOrderByLink4<>(this, function, false);
    }

    public OrderBy4<T1, T2, T3, T4> thenByDescending(SelectFunction4<T1, T2, T3, T4, ?> function) {
        return new ThenOrderByLink4<>(this, function, true);
    }

    private <V1> V1 aggregate(SelectFunction4<T1, T2, T3, T4, V1> function1) {
        return (V1) database().object(append(function1, List.of()));
    }

    public <V1, V2> Row2<V1, V2> aggregate(SelectFunction4<T1, T2, T3, T4, V1> function1, SelectFunction4<T1, T2, T3, T4, V2> function2) {
        return (Row2<V1, V2>) database().object(append(function1, List.of(function2)));
    }

    public <V1, V2, V3> Row3<V1, V2, V3> aggregate(SelectFunction4<T1, T2, T3, T4, V1> function1, SelectFunction4<T1, T2, T3, T4, V2> function2, SelectFunction4<T1, T2, T3, T4, V3> function3) {
        return (Row3<V1, V2, V3>) database().object(append(function1, List.of(function2, function3)));
    }

    public <V1, V2, V3, V4> Row4<V1, V2, V3, V4> aggregate(SelectFunction4<T1, T2, T3, T4, V1> function1, SelectFunction4<T1, T2, T3, T4, V2> function2, SelectFunction4<T1, T2, T3, T4, V3> function3, SelectFunction4<T1, T2, T3, T4, V4> function4) {
        return (Row4<V1, V2, V3, V4>) database().object(append(function1, List.of(function2, function3, function4)));
    }

    public <N1 extends Number> N1 sum(SelectFunction4<T1, T2, T3, T4, N1> function1) {
        return aggregate(Selects.sum(function1));
    }

    @SuppressWarnings("unused")
    public <N1 extends Number> Double avg(SelectFunction4<T1, T2, T3, T4, N1> function1) {
        return aggregate(Selects.avg(function1));
    }

    public <N1 extends Number> N1 min(SelectFunction4<T1, T2, T3, T4, N1> function1) {
        return aggregate(Selects.min(function1));
    }

    public <N1 extends Number> N1 max(SelectFunction4<T1, T2, T3, T4, N1> function1) {
        return aggregate(Selects.max(function1));
    }

    public Long count(SelectFunction4<T1, T2, T3, T4, ?> function1) {
        return aggregate(Selects.count(function1));
    }

    public Long count() {
        return count(null);
    }

    public Having4<T1, T2, T3, T4> having(QueryPredicate4<T1, T2, T3, T4> predicate) {
        return new HavingLink4<>(this, predicate);
    }

    private <L extends Link & Selection> L append(SelectFunction4<T1, T2, T3, T4, ?> first, List<SelectFunction4<T1, T2, T3, T4, ?>> rest) {
        var select = new FunctionSelectionLink(this, first);
        for (var function : rest) {
            select = new FunctionSelectionLink(select, function);
        }
        //noinspection unchecked
        return (L)select;
    }

}
