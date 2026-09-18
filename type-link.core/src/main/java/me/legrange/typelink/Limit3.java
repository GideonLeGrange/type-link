package me.legrange.typelink;

import java.util.List;

public sealed interface Limit3<T1, T2, T3> extends Clause permits And3, Distinct3, From3, GroupBy3, Join3, LimitLink3, Or3, OrderBy3, Where3 {

    List<Row3<T1, T2, T3>> list()  throws QueryException;

    <V1> List<V1> list(SelectFunction3<T1, T2, T3, V1> function1)  throws QueryException;

    <V1, V2> List<Row2<V1, V2>> list(SelectFunction3<T1, T2, T3, V1> function1,
                                     SelectFunction3<T1, T2, T3, V2> function2)  throws QueryException;

    <V1, V2, V3> List<Row3<V1, V2, V3>> list(SelectFunction3<T1, T2, T3, V1> function1,
                                             SelectFunction3<T1, T2, T3, V2> function2, SelectFunction3<T1, T2, T3, V3> function3)  throws QueryException;


    <V1, V2> Row2<V1, V2> aggregate(SelectFunction3<T1, T2, T3, V1> function1, SelectFunction3<T1, T2, T3, V2> function2)  throws QueryException;

    <V1, V2, V3> Row3<V1, V2, V3> aggregate(SelectFunction3<T1, T2, T3, V1> function1,
                                            SelectFunction3<T1, T2, T3, V2> function2,
                                            SelectFunction3<T1, T2, T3, V3> function3)  throws QueryException;

    <N1 extends Number> N1 sum(SelectFunction3<T1, T2, T3, N1> function1)  throws QueryException;

    @SuppressWarnings("unused")
    <N1 extends Number> Double avg(SelectFunction3<T1, T2, T3, Double> function1)  throws QueryException;

    <N1 extends Number> N1 min(SelectFunction3<T1, T2, T3, N1> function1)  throws QueryException;

    <N1 extends Number> N1 max(SelectFunction3<T1, T2, T3, N1> function1)  throws QueryException;

    Long count(SelectFunction3<T1, T2, T3, ?> function1)  throws QueryException;

    Long count()  throws QueryException;
}
