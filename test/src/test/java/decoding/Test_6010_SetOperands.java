package decoding;

import me.legrange.typelink.QueryPredicate1;
import me.legrange.typelink.TableMapper;
import me.legrange.typelink.sql.parser.QueryParseException;
import me.legrange.typelink.sql.parser.QueryParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Which side of a set test is the column.
 *
 * <p>{@code a.contains(b)} can mean the column is on either side - a list of values holding a
 * column, or a column holding a value - so the builder tries one and, failing that, the other.
 * It did so by swapping the operands and calling itself, which has no end when neither side is a
 * column: it swaps back and forth until the stack runs out.
 *
 * <p>That was reachable. An accessor declared on an interface the types share was not recognised as
 * a column until the check began asking about the table as well as the method, and a rule that
 * governs many types reaches members exactly that way - so a perfectly ordinary ownership test
 * ended in {@code StackOverflowError} rather than SQL. The recognition is fixed, and the recursion
 * behind it is what these tests are about: the same query shape with nothing column-like on either
 * side still has nowhere to go, and should say so.
 */
class Test_6010_SetOperands {

    public interface HasSale {
        int saleKey();
    }

    public record Invoice(int objKey, int saleKey, int amount) implements HasSale {
    }

    public record Sale(int saleKey, int clientId) {
    }

    private static final TableMapper<Record> BY_TYPE = new InterfaceMapper<>(Record.class);

    /** The column on the left, holding what a sub-select returns. */
    @Test
    void aColumnInWhatASubSelectReturns() {
        var db = new CapturingDatabase();

        db.from(Invoice.class)
                .where((QueryPredicate1<Invoice>) i -> db.from(Sale.class)
                        .where(s -> s.clientId() == 42)
                        .list(Sale::saleKey)
                        .contains(i.saleKey()))
                .list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.saleKey  IN"
                + " (SELECT Sale.saleKey FROM Sale WHERE Sale.clientId = ?)", db.sql());
    }

    /** The same, reached through an interface - the shape that used to overflow the stack. */
    @Test
    void aColumnReachedThroughAnInterface() {
        var db = new CapturingDatabase(BY_TYPE);

        var clause = QueryParser.parsePredicate(soldTo(db, 42), Invoice.class, BY_TYPE);

        assertInstanceOf(me.legrange.typelink.sql.structure.SqlInSet.class, clause);
    }

    private static <T extends HasSale> QueryPredicate1<T> soldTo(CapturingDatabase db, int clientId) {
        return row -> db.from(Sale.class)
                .where(s -> s.clientId() == clientId)
                .list(Sale::saleKey)
                .contains(((HasSale) row).saleKey());
    }

    /** A column on the right instead: the sides are tried both ways round. */
    @Test
    void aColumnOnTheRight() {
        var db = new CapturingDatabase();
        var keys = List.of(1, 2, 3);

        db.from(Invoice.class).where((QueryPredicate1<Invoice>) i -> keys.contains(i.saleKey())).list();

        assertEquals("SELECT Invoice.* FROM Invoice WHERE Invoice.saleKey  IN (?, ?, ?)", db.sql());
    }

    /** Neither side a column: nowhere to go, and it has to say so rather than run out of stack. */
    @Test
    void neitherSideAColumnIsRefused() {
        var db = new CapturingDatabase();
        var keys = List.of(1, 2, 3);
        var wanted = 2;

        var thrown = assertThrows(QueryParseException.class,
                () -> db.from(Invoice.class)
                        .where((QueryPredicate1<Invoice>) i -> keys.contains(wanted))
                        .list());

        assertEquals(true, thrown.getMessage().contains("neither side"),
                "expected a message naming the problem, got: " + thrown.getMessage());
    }
}
