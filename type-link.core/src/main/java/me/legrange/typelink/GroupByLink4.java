package me.legrange.typelink;


final class GroupByLink4<T1, T2, T3, T4> extends Link4<T1, T2, T3, T4> implements GroupByLink, GroupBy4<T1, T2, T3, T4> {

    public GroupByLink4(Link left, SelectFunction function) {
        super(left, function);
    }

}
