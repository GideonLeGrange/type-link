package me.legrange.typelink;

import java.util.List;

public sealed interface Limit2<T1, T2> extends Clause permits And2, Distinct2, GroupBy2, LimitLink2, Or2, OrderBy2, Where2 {

    List<Row2<T1, T2>> list() throws QueryException;

    <V1> List<V1> list(SelectFunction2<T1, T2, V1> function1) throws QueryException;

    <V1, V2> List<Row2<V1, V2>> list(SelectFunction2<T1, T2, V1> function1,
                                     SelectFunction2<T1, T2, V2> function2) throws QueryException;

    <V1, V2, V3> List<Row3<V1, V2, V3>> list(SelectFunction2<T1, T2, V1> function1,
                                             SelectFunction2<T1, T2, V2> function2, SelectFunction2<T1, T2, V3> function3) throws QueryException;


    <V1, V2> Row2<V1, V2> aggregate(SelectFunction2<T1, T2, V1> function1, SelectFunction2<T1, T2, V2> function2)  throws QueryException;

    <V1, V2, V3> Row3<V1, V2, V3> aggregate(SelectFunction2<T1, T2, V1> function1,
                                            SelectFunction2<T1, T2, V2> function2,
                                            SelectFunction2<T1, T2, V3> function3)  throws QueryException;

    <N1 extends Number> N1 sum(SelectFunction2<T1, T2, N1> function1)  throws QueryException;

    @SuppressWarnings("unused")
    <N1 extends Number> Double avg(SelectFunction2<T1, T2, Double> function1)  throws QueryException;

    <N1 extends Number> N1 min(SelectFunction2<T1, T2, N1> function1)  throws QueryException;

    <N1 extends Number> N1 max(SelectFunction2<T1, T2, N1> function1)  throws QueryException;

    Long count(SelectFunction2<T1, T2, ?> function1)  throws QueryException;

    Long count()  throws QueryException;


}
