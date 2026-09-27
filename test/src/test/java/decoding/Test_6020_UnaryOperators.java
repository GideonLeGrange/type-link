package decoding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unary negation ({@code -x}) used to crash the decoder instead of producing SQL.
 *
 * <p><b>The bug this guards.</b> {@link me.legrange.typelink.lambda.parser.BytecodeParser#operator}
 * used to pop two operands for every arithmetic opcode, on the assumption that all of them are
 * binary. But {@code INEG}/{@code LNEG}/{@code FNEG}/{@code DNEG} - what {@code -x} compiles to for
 * each numeric type - only ever push one operand, so the second {@code pop()} found the stack
 * already empty and threw a raw {@link java.util.EmptyStackException}, not one of this library's own
 * exceptions - and before the code ever reached the "unsupported opcode" check that would otherwise
 * cover it. This was not specific to the {@code validate-queries} plugin: it reproduced from plain
 * query execution, with no compile-time check involved.
 *
 * <p>Negation is deliberately supported rather than refused: every SQL dialect has a native way to
 * negate a value, and this library already turns the binary arithmetic operators ({@code +}, {@code
 * -}, {@code *}, {@code /}) into SQL, so there is no principled reason to treat the unary form as
 * unsupported. It decodes to {@link me.legrange.typelink.lambda.structure.Negate} and renders as
 * multiplication by {@code -1}, reusing the existing multiply rendering rather than adding a native
 * SQL unary-minus node. The {@code -1} itself is typed to match the negated value ({@code -1},
 * {@code -1L}, {@code -1.0f} or {@code -1.0}) rather than always being a plain {@code int} - the
 * database would coerce a mismatched type across the multiplication anyway, but {@link
 * me.legrange.typelink.lambda.structure.Negate} already carries the right type from the opcode, and
 * throwing it away would leave this the only synthesised constant in the codebase that does not
 * match the value it stands in for.
 */
class Test_6020_UnaryOperators {

    private record Numbers(int i, long l, float f, double d) {
    }

    @Test
    void negatedIntGeneratesMultiplyByMinusOne() {
        var db = new CapturingDatabase();
        db.from(Numbers.class).where(n -> -n.i() > 0).list();

        assertEquals("SELECT Numbers.* FROM Numbers WHERE ? * Numbers.i > ?", db.sql());
        assertEquals(List.of(-1, 0), db.params());
    }

    @Test
    void negatedLongGeneratesMultiplyByMinusOne() {
        var db = new CapturingDatabase();
        db.from(Numbers.class).where(n -> -n.l() > 0).list();

        assertEquals("SELECT Numbers.* FROM Numbers WHERE ? * Numbers.l > ?", db.sql());
        // Both the multiplier and the comparison's 0 are long: the multiplier matches Negate's own
        // type rather than always being a plain int.
        assertEquals(List.of(-1L, 0L), db.params());
    }

    @Test
    void negatedFloatGeneratesMultiplyByMinusOne() {
        var db = new CapturingDatabase();
        db.from(Numbers.class).where(n -> -n.f() > 0).list();

        assertEquals("SELECT Numbers.* FROM Numbers WHERE ? * Numbers.f > ?", db.sql());
        assertEquals(List.of(-1.0f, 0.0f), db.params());
    }

    @Test
    void negatedDoubleGeneratesMultiplyByMinusOne() {
        var db = new CapturingDatabase();
        db.from(Numbers.class).where(n -> -n.d() > 0).list();

        assertEquals("SELECT Numbers.* FROM Numbers WHERE ? * Numbers.d > ?", db.sql());
        assertEquals(List.of(-1.0, 0.0), db.params());
    }
}
