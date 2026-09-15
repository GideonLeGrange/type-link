package decoding;

import me.legrange.typelink.QueryPredicate1;
import me.legrange.typelink.sql.parser.QueryParseException;
import org.junit.jupiter.api.Test;
import rec.Client;
import rec.Invoice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Characterisation test: which ways of building a predicate the decoder accepts, and which it does
 * not.
 *
 * <p>This pins down a boundary rather than guarding a fixed bug. Callers who build predicates
 * outside the query expression - an access control layer deciding a row filter per user, say - need
 * to know that a predicate may be <em>returned</em> by a helper but not <em>called from inside</em>
 * another lambda, and that composition therefore goes through {@code and()} / {@code or()} rather
 * than Java's {@code &&} and {@code ||}.
 *
 * <p><b>The negative cases below assert a limitation, not a desired behaviour.</b> If a change to
 * the decoder makes one of them work, that is an improvement: update the expectation to the SQL it
 * now produces, and do not simply delete the case. If one of them starts failing with a
 * <em>different</em> exception, check the decoder is still refusing it for the right reason - the
 * dangerous outcome is not an exception but a predicate that quietly compiles to something weaker
 * than it reads.
 */
class Test_5020_PredicateComposition {

    // --- accepted -----------------------------------------------------------

    @Test
    void predicateBuiltElsewhere() {
        QueryPredicate1<Invoice> owned = i -> i.clientId() == 7L;

        var db = new CapturingDatabase();
        db.from(Invoice.class).where(owned).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.clientId = ?", db.sql());
        assertEquals(List.of(7L), db.params());
    }

    @Test
    void predicateFromFactory() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(ownedBy(7L)).list();

        assertEquals(List.of(7L), db.params());
    }

    @Test
    void orComposesTwoPreBuiltPredicates() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(isPaid()).or(ownedBy(7L)).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.paid = ? OR Invoice.clientId = ?", db.sql());
        assertEquals(List.of(true, 7L), db.params());
    }

    @Test
    void andComposesTwoPreBuiltPredicates() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(isPaid()).and(ownedBy(7L)).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.paid = ? AND Invoice.clientId = ?", db.sql());
        assertEquals(List.of(true, 7L), db.params());
    }

    @Test
    void subSelectPredicateComposes() {
        var db = new CapturingDatabase();
        QueryPredicate1<Invoice> ownedViaClient = i -> db.from(Client.class)
                .where(c -> c.name().startsWith("Acme"))
                .list(Client::id)
                .contains(i.clientId());

        db.from(Invoice.class).where(isPaid()).or(ownedViaClient).list();

        assertEquals("""
                SELECT Invoice.* FROM Invoice WHERE Invoice.paid = ? \
                OR Invoice.clientId  IN (SELECT Client.id FROM Client WHERE Client.name LIKE ?)""", db.sql());
        assertEquals(List.of(true, "Acme%"), db.params());
    }

    @Test
    void callNotDependingOnTheRowIsEvaluatedToABindParameter() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(i -> i.amount() > threshold()).list();

        // Correct: the call cannot vary per row, so resolving it once at build time is right.
        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.amount > ?", db.sql());
        assertEquals(List.of(100.0), db.params());
    }

    // --- refused: see the class javadoc before changing these ----------------

    @Test
    void callingAPredicateFromInsideALambdaIsRefused() {
        QueryPredicate1<Invoice> owned = i -> i.clientId() == 7L;
        var db = new CapturingDatabase();

        assertThrows(QueryParseException.class,
                () -> db.from(Invoice.class).where(i -> owned.test(i)).list(),
                "composing predicates in Java must fail loudly; use where(a).or(b) instead");
    }

    @Test
    void composingWithJavaOrIsRefused() {
        QueryPredicate1<Invoice> owned = i -> i.clientId() == 7L;
        var db = new CapturingDatabase();

        assertThrows(QueryParseException.class,
                () -> db.from(Invoice.class).where(i -> i.paid() || owned.test(i)).list());
    }

    @Test
    void callDependingOnTheRowIsRefused() {
        var db = new CapturingDatabase();

        // The value varies per row, so it cannot be resolved at build time. Refusing is the only
        // safe answer: folding it to a constant would silently weaken the predicate.
        assertThrows(QueryParseException.class,
                () -> db.from(Invoice.class).where(i -> i.clientId() == derivedFrom(i)).list());
    }

    // --- helpers -------------------------------------------------------------

    private static QueryPredicate1<Invoice> ownedBy(long clientNumber) {
        return i -> i.clientId() == clientNumber;
    }

    private static QueryPredicate1<Invoice> isPaid() {
        return Invoice::paid;
    }

    private static double threshold() {
        return 100.0;
    }

    private static long derivedFrom(Invoice invoice) {
        return invoice.clientId() + 1;
    }
}
