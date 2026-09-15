package me.legrange.typelink.sql.structure;

public sealed interface SqlSetOperator extends SqlRelationalOperator permits SqlInSet, SqlNotInSet {

}
