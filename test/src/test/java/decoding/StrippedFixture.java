package decoding;

import me.legrange.typelink.QueryPredicate1;
import rec.Invoice;

/**
 * Lambda fixtures for {@link Test_5010_DecodingWithoutDebugInfo}, and for nothing else.
 *
 * <p><b>Do not decode these predicates from any other test.</b> The decoder caches parsed class
 * models in a static map keyed by class name, so whichever test decodes a lambda from this class
 * first fixes the bytes every later test sees. {@code Test_5010} needs to be the one that gets
 * there, with its stripped-bytes class loader installed, or it silently tests nothing.
 */
final class StrippedFixture {

    /** Single narrow capture: the simplest thing that still needs an argument slot resolved. */
    static QueryPredicate1<Invoice> ownedBy(long clientNumber) {
        return i -> i.clientId() == clientNumber;
    }

    /** Two captures, one wide, so the position/slot distinction is exercised here too. */
    static QueryPredicate1<Invoice> ownedByAbove(long clientNumber, int amount) {
        return i -> i.clientId() == clientNumber && i.amount() > amount;
    }

    /** A method reference, which decodes to an instance method whose slot 0 is {@code this}. */
    static QueryPredicate1<Invoice> isPaid() {
        return Invoice::paid;
    }

    private StrippedFixture() {
    }
}
