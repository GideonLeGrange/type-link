package me.legrange.typelink;

import java.io.Serializable;
import java.util.List;

abstract sealed class Link3<T1, T2, T3> extends Link permits AndLink3, DistinctLink3, FromLink3, GroupByLink3, HavingLink3, JoinLink3, LimitLink3, OrderByLink3, OrLink3, ThenOrderByLink3, WhereLink3 {

    protected Link3(Link left, Serializable serializable) {
        super(left, serializable);
    }

    public Where3<T1, T2, T3> where(QueryPredicate3<T1, T2, T3> clause) {
        return new WhereLink3<>(this, clause);
    }

    public GroupBy3<T1, T2, T3> groupBy(SelectFunction3<T1, T2, T3, ?> function) {
        return new GroupByLink3<>(this, function);
    }

    public GroupBy3<T1, T2, T3> groupBy(SelectFunction3<T1, T2, T3, ?> function1, SelectFunction3<T1, T2, T3, ?> function2) {
        return new GroupByLink3<>(new GroupByLink2<>(this, function1), function2);
    }

    public GroupBy3<T1, T2, T3> groupBy(SelectFunction3<T1, T2, T3, ?> function1, SelectFunction3<T1, T2, T3, ?> function2, SelectFunction3<T1, T2, T3, ?> function3) {
        return new GroupByLink3<>(new GroupByLink3<>(new GroupByLink2<>(this, function1), function2), function3);
    }

    public OrderBy3<T1, T2, T3> orderBy(SelectFunction3<T1, T2, T3, ?> function) {
        return new ThenOrderByLink3<>(this, function, false);
    }

    public Limit3<T1, T2, T3> limit(int limit) {
        return limit(limit, 0);
    }

    public Distinct3<T1, T2, T3> distinct() {
        return new DistinctLink3<>(this);
    }

    public Limit3<T1, T2, T3> limit(int limit, int offset) {
        return new LimitLink3<>(this, limit, offset);
    }

    public And3<T1, T2, T3> and(QueryPredicate3<T1, T2, T3> clause) {
        return new AndLink3<>(this, clause);
    }

    public Or3<T1, T2, T3> or(QueryPredicate3<T1, T2, T3> clause) {
        return new OrLink3<>(this, clause);
    }

    public OrderBy3<T1, T2, T3> orderByDescending(SelectFunction3<T1, T2, T3, ?> function) {
        return new ThenOrderByLink3<>(this, function, true);
    }

    public <V1> List<V1> list(SelectFunction3<T1, T2, T3, V1> function1) {
        return database().list(new FunctionSelectionLink(this, function1));
    }

    public List<Row3<T1, T2, T3>> list() {
        return database().list(new FunctionSelectionLink(this, new Select3<>(Selector.Type.LIST_ROW, Row3.class, (T1 _, T2 _, T3 _) -> null)));
    }

    public <V1, V2> List<Row2<V1, V2>> list(SelectFunction3<T1, T2, T3, V1> function1, SelectFunction3<T1, T2, T3, V2> function2) {
        return database().list(new FunctionSelectionLink(new FunctionSelectionLink(this, function1), function2));
    }

    public <V1, V2, V3> List<Row3<V1, V2, V3>> list(SelectFunction3<T1, T2, T3, V1> function1, SelectFunction3<T1, T2, T3, V2> function2, SelectFunction3<T1, T2, T3, V3> function3) {
        return database().list(new FunctionSelectionLink(new FunctionSelectionLink(new FunctionSelectionLink(this, function1), function2), function3));
    }

    public OrderBy3<T1, T2, T3> thenBy(SelectFunction3<T1, T2, T3, ?> function) {
        return new ThenOrderByLink3<>(this, function, false);
    }

    public OrderBy3<T1, T2, T3> thenByDescending(SelectFunction3<T1, T2, T3, ?> function) {
        return new ThenOrderByLink3<>(this, function, true);
    }

    private <V1> V1 aggregate(SelectFunction3<T1, T2, T3, V1> function1) {
        return (V1) database().object(append(function1, List.of()));
    }

    public <V1, V2> Row2<V1, V2> aggregate(SelectFunction3<T1, T2, T3, V1> function1, SelectFunction3<T1, T2, T3, V2> function2) {
        return (Row2<V1, V2>) database().object(append(function1, List.of(function2)));
    }

    public <V1, V2, V3> Row3<V1, V2, V3> aggregate(SelectFunction3<T1, T2, T3, V1> function1, SelectFunction3<T1, T2, T3, V2> function2, SelectFunction3<T1, T2, T3, V3> function3) {
        return (Row3<V1, V2, V3>) database().object(append(function1, List.of(function2, function3)));
    }

    public <N1 extends Number> N1 sum(SelectFunction3<T1, T2, T3, N1> function1) {
        return aggregate(Selects.sum(function1));
    }

    @SuppressWarnings("unused")
    public <N1 extends Number> Double avg(SelectFunction3<T1, T2, T3, Double> function1) {
        return aggregate(Selects.avg(function1));
    }

    public <N1 extends Number> N1 min(SelectFunction3<T1, T2, T3, N1> function1) {
        return aggregate(Selects.min(function1));
    }

    public <N1 extends Number> N1 max(SelectFunction3<T1, T2, T3, N1> function1) {
        return aggregate(Selects.max(function1));
    }

    public Long count(SelectFunction3<T1, T2, T3, ?> function1) {
        return aggregate(Selects.count(function1));
    }

    public Long count() {
        return count(null);
    }

    public Having3<T1, T2, T3> having(QueryPredicate3<T1, T2, T3> predicate) {
        return new HavingLink3<>(this, predicate);
    }

    private <L extends Link & Selection> L append(SelectFunction3<T1, T2, T3, ?> first, List<SelectFunction3<T1, T2, T3, ?>> rest) {
        var select = new FunctionSelectionLink(this, first);
        for (var function : rest) {
            select = new FunctionSelectionLink(select, function);
        }
        //noinspection unchecked
        return (L)select;
    }

}
