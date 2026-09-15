package me.legrange.typelink;

public sealed interface WhereLink extends Clause permits WhereLink1, WhereLink2, WhereLink3, WhereClause {
}
