package me.legrange.typelink;

import java.util.List;

public sealed interface Limit4<T1, T2, T3, T4> extends Clause permits And4, Distinct4, From4, GroupBy4, Join4, LimitLink4, Or4, OrderBy4, Where4 {

    List<Row4<T1, T2, T3, T4>> list()  throws QueryException;

    <V1> List<V1> list(SelectFunction4<T1, T2, T3, T4, V1> function1)  throws QueryException;

    <V1, V2> List<Row2<V1, V2>> list(SelectFunction4<T1, T2, T3, T4, V1> function1,
                                     SelectFunction4<T1, T2, T3, T4, V2> function2)  throws QueryException;

    <V1, V2, V3> List<Row3<V1, V2, V3>> list(SelectFunction4<T1, T2, T3, T4, V1> function1,
                                             SelectFunction4<T1, T2, T3, T4, V2> function2, SelectFunction4<T1, T2, T3, T4, V3> function3)  throws QueryException;


    <V1, V2, V3, V4> List<Row4<V1, V2, V3, V4>> list(SelectFunction4<T1, T2, T3, T4, V1> function1,
                                                     SelectFunction4<T1, T2, T3, T4, V2> function2,
                                                     SelectFunction4<T1, T2, T3, T4, V3> function3,
                                                     SelectFunction4<T1, T2, T3, T4, V4> function4)  throws QueryException;

    <V1, V2> Row2<V1, V2> aggregate(SelectFunction4<T1, T2, T3, T4, V1> function1, SelectFunction4<T1, T2, T3, T4, V2> function2)  throws QueryException;

    <V1, V2, V3> Row3<V1, V2, V3> aggregate(SelectFunction4<T1, T2, T3, T4, V1> function1,
                                            SelectFunction4<T1, T2, T3, T4, V2> function2,
                                            SelectFunction4<T1, T2, T3, T4, V3> function3)  throws QueryException;

    <V1, V2, V3, V4> Row4<V1, V2, V3, V4> aggregate(SelectFunction4<T1, T2, T3, T4, V1> function1,
                                                    SelectFunction4<T1, T2, T3, T4, V2> function2,
                                                    SelectFunction4<T1, T2, T3, T4, V3> function3,
                                                    SelectFunction4<T1, T2, T3, T4, V4> function4)  throws QueryException;

    <N1 extends Number> N1 sum(SelectFunction4<T1, T2, T3, T4, N1> function1)  throws QueryException;

    @SuppressWarnings("unused")
    <N1 extends Number> Double avg(SelectFunction4<T1, T2, T3, T4, N1> function1)  throws QueryException;

    <N1 extends Number> N1 min(SelectFunction4<T1, T2, T3, T4, N1> function1)  throws QueryException;

    <N1 extends Number> N1 max(SelectFunction4<T1, T2, T3, T4, N1> function1)  throws QueryException;

    Long count(SelectFunction4<T1, T2, T3, T4, ?> function1)  throws QueryException;

    Long count()  throws QueryException;
}
