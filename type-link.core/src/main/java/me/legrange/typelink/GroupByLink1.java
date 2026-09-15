package me.legrange.typelink;

final class GroupByLink1<T1> extends Link1<T1> implements GroupByLink, GroupBy1<T1> {

    GroupByLink1(Link left, SelectFunction function) {
        super(left, function);
    }

}
