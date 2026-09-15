package me.legrange.typelink;

import java.io.Serializable;

public sealed interface QueryPredicate extends Serializable permits QueryPredicate1, QueryPredicate2, QueryPredicate3 {
}
