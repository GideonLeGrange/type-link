package me.legrange.typelink;

import me.legrange.typelink.JoinLink.Type;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("unchecked")
abstract sealed class Link1<T1> extends Link permits AndLink1, DistinctLink1, FromLink1, GroupByLink1, HavingLink1, LimitLink1, OrderByLink1, OrLink1, ThenOrderByLink1, WhereLink1 {

    protected Link1(Link left, Serializable serializable) {
        super(left, serializable);
    }

    public final Limit1<T1> limit(int limit) {
        return limit(limit, 0);
    }

    public Distinct1<T1> distinct() {
        return new DistinctLink1<>(this);
    }

    public final Limit1<T1> limit(int limit, int offset) {
        return new LimitLink1<>(this, limit, offset);
    }

    public GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function) {
        return new GroupByLink1<>(this, function);
    }

    public GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function1, SelectFunction1<T1, ?> function2) {
        return new GroupByLink1<>(new GroupByLink1<>(this, function1), function2);
    }

    public GroupBy1<T1> groupBy(SelectFunction1<T1, ?> function1, SelectFunction1<T1, ?> function2, SelectFunction1<T1, ?> function3) {
        return new GroupByLink1<>(new GroupByLink1<>(new GroupByLink1<>(this, function1), function2), function3);
    }

    public OrderBy1<T1> orderBy(SelectFunction1<T1, ?> function) {
        return new ThenOrderByLink1<>(this, function, false);
    }

    public OrderBy1<T1> orderByDescending(SelectFunction1<T1, ?> function) {
        return new ThenOrderByLink1<>(this, function, true);
    }

    public <T2> Join2<T1, T2> join(Class<T2> type, QueryPredicate2<T1, T2> function) {
        return new JoinLink2<>(this, type, function);
    }

    public <T2> Join2<T1, T2> leftJoin(Class<T2> type, QueryPredicate2<T1, T2> function) {
        return new JoinLink2<>(this, type, Type.LEFT_OUTER, function);
    }

    public <T2> Join2<T1, T2> rightJoin(Class<T2> type, QueryPredicate2<T1, T2> function) {
        return new JoinLink2<>(this, type, Type.RIGHT_OUTER, function);
    }

    public <T2> Join2<T1, T2> fullJoin(Class<T2> type, QueryPredicate2<T1, T2> function) {
        return new JoinLink2<>(this, type, Type.FULL_OUTER, function);
    }

    public Where1<T1> where(QueryPredicate1<T1> clause) {
        return new WhereLink1<>(this, clause);
    }

    public And1<T1> and(QueryPredicate1<T1> clause) {
        return new AndLink1<>(this, clause);
    }

    public Or1<T1> or(QueryPredicate1<T1> clause) {
        return new OrLink1<>(this, clause);
    }

    public OrderBy1<T1> thenBy(SelectFunction1<T1, ?> function) {
        return new ThenOrderByLink1<>(this, function, false);
    }

    public OrderBy1<T1> thenByDescending(SelectFunction1<T1, ?> function) {
        return new ThenOrderByLink1<>(this, function, true);
    }


    public List<T1> list() {
        return list(new Select1<>(Selector.Type.LIST_ROW, t -> t));
    }

    public <V1> List<V1> list(SelectFunction1<T1, V1> selector) throws QueryException {
        return database().list(new FunctionSelectionLink(this, selector));
    }

    public <V1, V2> List<Row2<V1, V2>> list(SelectFunction1<T1, V1> function1, SelectFunction1<T1, V2> function2) {
        return database().list(append(function1, List.of(function2)));
    }

    public final <V1, V2, V3> List<Row3<V1, V2, V3>> list(SelectFunction1<T1, V1> function1,
                                                          SelectFunction1<T1, V2> function2,
                                                          SelectFunction1<T1, V3> function3) {
        return database().list(append(function1, List.of(function2, function3)));
    }

    public <V1> V1 aggregate(SelectFunction1<T1, V1> selectFunction1) {
        return (V1) database().object(new FunctionSelectionLink(this, selectFunction1));
    }

    public <V1, V2> Row2<V1, V2> aggregate(SelectFunction1<T1, V1> function1, SelectFunction1<T1, V2> function2) {
        return (Row2<V1, V2>) database().object(append(function1, List.of(function2)));
    }

    public <V1, V2, V3> Row3<V1, V2, V3> aggregate(SelectFunction1<T1, V1> function1, SelectFunction1<T1, V2> function2,
                                                   SelectFunction1<T1, V3> function3) {
        return (Row3<V1, V2, V3>) database().object(append(function1, List.of(function2, function3)));
    }

    public <N1 extends Number> N1 sum(SelectFunction1<T1, N1> function1) {
        return aggregate(Selects.sum(function1));
    }

    public <N1 extends Number> N1 min(SelectFunction1<T1, N1> function1) {
        return aggregate(Selects.min(function1));
    }

    public <N1 extends Number> N1 max(SelectFunction1<T1, N1> function1) {
        return aggregate(Selects.max(function1));
    }

    @SuppressWarnings("unused")
    public <N1 extends Number> Double avg(SelectFunction1<T1, Double> function1) {
        return aggregate(Selects.avg(function1));
    }

    public Long count(SelectFunction1<T1, ?> function1) {
        return aggregate(Selects.count(function1));
    }

    public Long count() {
        return count(null);
    }

    public Having1<T1> having(QueryPredicate1<T1> predicate) {
        return new HavingLink1<>(this, predicate);
    }

    private <L extends Link & Selection> L append(SelectFunction1<T1, ?> first, List<SelectFunction1<T1, ?>> rest) {
        var select = new FunctionSelectionLink(this, first);
        for (var function : rest) {
            select = new FunctionSelectionLink(select, function);
        }
        //noinspection unchecked
        return (L) select;
    }
}
