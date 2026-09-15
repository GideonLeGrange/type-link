package me.legrange.typelink;

import java.util.List;

public sealed interface Limit1<T1> extends Clause permits And1, Distinct1, GroupBy1, LimitLink1, Or1, OrderBy1, Where1 {

    List<T1> list() throws QueryException;

    <V1> List<V1> list(SelectFunction1<T1, V1> selector) throws QueryException;

    <V1, V2> List<Row2<V1, V2>> list(SelectFunction1<T1, V1> function1,
                                     SelectFunction1<T1, V2> function2) throws QueryException;

    <V1, V2, V3> List<Row3<V1, V2, V3>> list(SelectFunction1<T1, V1> function1,
                                             SelectFunction1<T1, V2> function2,
                                             SelectFunction1<T1, V3> function3) throws QueryException;

    <V1> V1 aggregate(SelectFunction1<T1, V1> function1) throws QueryException;

    <V1, V2> Row2<V1, V2> aggregate(SelectFunction1<T1, V1> function1, SelectFunction1<T1, V2> function2) throws QueryException;

    <V1, V2, V3> Row3<V1, V2, V3> aggregate(SelectFunction1<T1, V1> function1,
                                            SelectFunction1<T1, V2> function2,
                                            SelectFunction1<T1, V3> function3) throws QueryException;

    <N1 extends Number> N1 sum(SelectFunction1<T1, N1> function1) throws QueryException;

    @SuppressWarnings("unused")
    <N1 extends Number> Double avg(SelectFunction1<T1, Double> function1) throws QueryException;

    <N1 extends Number> N1 min(SelectFunction1<T1, N1> function1) throws QueryException;

    <N1 extends Number> N1 max(SelectFunction1<T1, N1> function1) throws QueryException;

    Long count(SelectFunction1<T1, ?> function1) throws QueryException;

    Long count() throws QueryException;

}
