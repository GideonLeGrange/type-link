package me.legrange.typelink;

import java.util.List;

public sealed interface FromLink extends Clause permits FromClause, FromLink1, FromLink2, FromLink3, FromLink4 {
    List<Class<?>> types();
}
