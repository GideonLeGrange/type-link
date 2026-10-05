package decoding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An ordinary local variable inside a block-bodied lambda should decode like the equivalent
 * single-expression lambda - naming an intermediate result changes nothing about the query.
 *
 * <p><b>The bug this targets.</b> {@code BytecodeParser.store()} only recognises two opcodes:
 * {@code ASTORE_1} (an object local stored to slot 1) and {@code ISTORE_2} (an int local stored to
 * slot 2). Every other store - {@code ISTORE_1}, {@code ISTORE_3}, any {@code LSTORE}/{@code
 * FSTORE}/{@code DSTORE}, an object local in any slot but 1 - falls through to "unsupported opcode".
 * This has nothing to do with loops or mutation; both lambdas below declare a local, assign it once,
 * and never touch it again. Which slot a local lands in is decided by how many parameters and
 * captures come before it, not by anything the lambda's author wrote - which is why today's failure
 * reads as arbitrary rather than as a deliberate restriction.
 *
 * <p><b>These tests currently fail.</b> Today, both throw {@code BytecodeParseException} instead of
 * producing the SQL asserted below. They are written against the fix, not against today's behaviour,
 * so they go green exactly when local variables decode correctly - there is no wording to update
 * first the way there is in {@code Test_6060_CrypticUnsupportedConstructMessages}.
 */
class Test_6040_LocalVariablesInLambdaBody {

    private record Numbers(int count) {
    }

    @Test
    void aSingleLocalVariableDecodesLikeTheInlineExpression() {
        var db = new CapturingDatabase();

        db.from(Numbers.class).where(n -> {
            var x = n.count();
            return x > 0;
        }).list();

        assertEquals("SELECT Numbers.count FROM Numbers WHERE Numbers.count > ?", db.sql());
        assertEquals(List.of(0), db.params());
    }

    @Test
    void aSecondLocalVariableAlsoDecodesLikeTheInlineExpression() {
        var db = new CapturingDatabase();

        // Two locals instead of one: still nothing more than naming intermediate values, equivalent
        // to `n.count() * 2 > 0`.
        db.from(Numbers.class).where(n -> {
            var doubled = n.count() * 2;
            var isPositive = doubled > 0;
            return isPositive;
        }).list();

        assertEquals("SELECT Numbers.count FROM Numbers WHERE Numbers.count * ? > ?", db.sql());
        assertEquals(List.of(2, 0), db.params());
    }
}
