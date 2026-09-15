package decoding;

import me.legrange.typelink.QueryPredicate1;
import org.junit.jupiter.api.Test;
import rec.Invoice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test: captured lambda arguments must be resolved by their position in the parameter
 * list, never by their JVM local variable slot.
 *
 * <p><b>The bug this guards.</b> {@code ClauseBuilder} and {@code Values} used to resolve captured
 * values with {@code context.args().get(arg.slot())}. A slot number is not a capture position:
 * {@code long} and {@code double} occupy two slots each, so the two numberings diverge as soon as a
 * wide capture appears. Capturing {@code (long, int)} threw {@code ArrayIndexOutOfBoundsException},
 * because the trailing capture's slot landed past the end of the captured argument list.
 *
 * <p><b>What a regression looks like.</b> Either an {@code ArrayIndexOutOfBoundsException} out of
 * the parser, or - worse and not observed, but the reason the assertions below check values rather
 * than only that a query was produced - bind parameters carrying a neighbouring capture's value.
 * Assert on {@code params()}, not just on the SQL text: the SQL is identical either way.
 *
 * <p>Each case names its captured values inline so a misread shows up as a wrong number rather than
 * as an exception. The all-narrow case is the control: it cannot desync and must always have passed.
 */
class Test_5000_CapturedArgumentPositions {

    @Test
    void narrowCaptureOnly() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(narrow(11, 22)).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.amount > ? AND Invoice.id = ?", db.sql());
        assertEquals(List.of(11, 22), db.params());
    }

    @Test
    void trailingWideCapture() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(narrowThenWide(11, 22L)).list();

        assertEquals(List.of(11, 22L), db.params());
    }

    @Test
    void leadingWideCapture() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(wideThenNarrow(11L, 22)).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.clientId = ? AND Invoice.amount > ?", db.sql());
        assertEquals(List.of(11L, 22), db.params());
    }

    @Test
    void twoWideCaptures() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(wideThenWide(11.5d, 22L)).list();

        assertEquals(List.of(11.5d, 22L), db.params());
    }

    @Test
    void singleWideCapture() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(singleWide(11.5d)).list();

        assertEquals(List.of(11.5d), db.params());
    }

    @Test
    void threeCapturesLedByWide() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(three(11L, 22, 33)).list();

        assertEquals(List.of(11L, 22, 33), db.params());
    }

    @Test
    void fourCapturesLedByWide() {
        var db = new CapturingDatabase();
        db.from(Invoice.class).where(four(11L, 22, 33, 44)).list();

        assertEquals(List.of(11L, 22, 33, 44), db.params());
    }

    // Built in factory methods so the captures really are captures: a lambda written inline in the
    // test method would close over locals the compiler may treat differently.

    private static QueryPredicate1<Invoice> narrow(int a, int b) {
        return i -> i.amount() > a && i.id() == b;
    }

    private static QueryPredicate1<Invoice> narrowThenWide(int a, long b) {
        return i -> i.amount() > a && i.clientId() == b;
    }

    private static QueryPredicate1<Invoice> wideThenNarrow(long a, int b) {
        return i -> i.clientId() == a && i.amount() > b;
    }

    private static QueryPredicate1<Invoice> wideThenWide(double a, long b) {
        return i -> i.amount() > a && i.clientId() == b;
    }

    private static QueryPredicate1<Invoice> singleWide(double a) {
        return i -> i.amount() > a;
    }

    private static QueryPredicate1<Invoice> three(long a, int b, int c) {
        return i -> i.clientId() == a && i.amount() > b && i.id() == c;
    }

    private static QueryPredicate1<Invoice> four(long a, int b, int c, int d) {
        return i -> i.clientId() == a && i.amount() > b && i.id() == c && i.amount() < d;
    }
}
