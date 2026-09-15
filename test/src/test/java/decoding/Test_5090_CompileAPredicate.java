package decoding;

import me.legrange.typelink.QueryPredicate1;
import me.legrange.typelink.TableMapper;
import me.legrange.typelink.sql.parser.QueryParser;
import me.legrange.typelink.sql.structure.SqlAnd;
import me.legrange.typelink.sql.structure.SqlConstant;
import me.legrange.typelink.sql.structure.SqlEq;
import me.legrange.typelink.sql.structure.SqlSubSelect;
import me.legrange.typelink.sql.structure.SqlTableColumn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compiling one predicate on its own, without a query around it.
 *
 * <p>{@code parseChain} turns a builder chain into a whole query, which is what a caller writing a
 * query needs. Something that already holds a query and wants to add a condition to it has neither
 * a chain nor a database to build one from - it has an {@link me.legrange.typelink.sql.structure.SqlQuery}
 * and a predicate. That is the shape of a rule saying which rows of a table may be read.
 */
class Test_5090_CompileAPredicate {

    public record Invoice(int objKey, int clientId, int amount, boolean paid) {
    }

    public record Sale(int objKey, int invoiceKey, int clientId) {
    }

    private static final TableMapper<Record> MAPPER = TableMapper.RECORD_MAPPER;

    @Test
    void compilesAComparisonAgainstItsTable() {
        var clause = QueryParser.parsePredicate(
                (QueryPredicate1<Invoice>) i -> i.clientId() == 42, Invoice.class, MAPPER);

        var eq = assertInstanceOf(SqlEq.class, clause);
        var column = assertInstanceOf(SqlTableColumn.class, eq.left());
        assertEquals("Invoice", column.tableName());
        assertEquals("clientId", column.name());
        assertEquals(42, assertInstanceOf(SqlConstant.class, eq.right()).value());
    }

    /** A predicate built elsewhere, carrying what it captured - how a rule hands one over. */
    @Test
    void compilesAPredicateBuiltElsewhere() {
        var clause = QueryParser.parsePredicate(ownedBy(42), Invoice.class, MAPPER);

        var eq = assertInstanceOf(SqlEq.class, clause);
        assertEquals(42, assertInstanceOf(SqlConstant.class, eq.right()).value());
    }

    private static QueryPredicate1<Invoice> ownedBy(int clientId) {
        return i -> i.clientId() == clientId;
    }

    @Test
    void compilesAConjunction() {
        var clause = QueryParser.parsePredicate(
                (QueryPredicate1<Invoice>) i -> i.clientId() == 42 && i.paid(), Invoice.class, MAPPER);

        assertInstanceOf(SqlAnd.class, clause);
    }

    /** Ownership through a link is a sub-select, and has to survive this route too. */
    @Test
    void compilesACorrelatedSubSelect() {
        var db = new CapturingDatabase();

        var clause = QueryParser.parsePredicate(
                (QueryPredicate1<Invoice>) i -> db.from(Sale.class)
                        .where(s -> s.invoiceKey() == i.objKey() && s.clientId() == 42)
                        .count() > 0,
                Invoice.class, MAPPER);

        assertTrue(clause instanceof me.legrange.typelink.sql.structure.SqlGt
                        || clause instanceof me.legrange.typelink.sql.structure.SqlLt,
                "a count compared to zero is a relational operator, got " + clause.getClass().getSimpleName());
        assertTrue(containsSubSelect(clause), "the ownership query has to survive as a sub-select");
    }

    private static boolean containsSubSelect(Object part) {
        return switch (part) {
            case SqlSubSelect _ -> true;
            case me.legrange.typelink.sql.structure.SqlGt(var left, var right) ->
                    containsSubSelect(left) || containsSubSelect(right);
            case me.legrange.typelink.sql.structure.SqlLt(var left, var right) ->
                    containsSubSelect(left) || containsSubSelect(right);
            default -> false;
        };
    }
}
