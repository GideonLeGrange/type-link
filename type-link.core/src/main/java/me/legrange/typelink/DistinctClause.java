package me.legrange.typelink;

import me.legrange.typelink.lambda.structure.Lambda;

/**
 * Reconstructed {@code distinct()} link, used when a sub-query chain is rebuilt from a
 * method-call tree (see {@link me.legrange.typelink.sql.parser.SubQueryParser}).
 */
public final class DistinctClause extends Link implements DistinctLink {

    public DistinctClause(Link left) {
        super(left, (Lambda) null);
    }

}
