package me.legrange.typelink;

final class GroupByLink2<T1, T2> extends Link2<T1, T2> implements GroupByLink, GroupBy2<T1, T2> {

    public GroupByLink2(Link left, SelectFunction function) {
        super(left, function);
    }


}
