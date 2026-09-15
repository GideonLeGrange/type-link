package me.legrange.typelink;

public sealed interface OrderByLink extends Clause permits OrderByLink1, OrderByLink2, OrderByLink3, OrderClause, ThenOrderByLink1, ThenOrderByLink2, ThenOrderByLink3 {

    boolean reversed();

}
