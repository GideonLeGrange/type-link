package me.legrange.typelink;


final class GroupByLink3<T1, T2, T3> extends Link3<T1, T2, T3> implements GroupByLink, GroupBy3<T1, T2, T3> {

    public GroupByLink3(Link left, SelectFunction function) {
        super(left, function);
    }

}
