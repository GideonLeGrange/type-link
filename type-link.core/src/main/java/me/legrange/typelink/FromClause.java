package me.legrange.typelink;


import me.legrange.typelink.lambda.structure.Lambda;

import java.util.List;

public final class FromClause extends Link implements FromLink {

    private final List<Class<?>> types;

    public FromClause(List<Class<?>> types) {
        super(null, (Lambda) null);
        this.types = types;
    }

    @Override
    public List<Class<?>> types() {
        return types;
    }
}
