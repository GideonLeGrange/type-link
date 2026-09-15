package decoding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Values the compiler pushes and then throws away.
 *
 * <p>Bytecode discards a value with {@code POP}, or with {@code POP2} where the value is a
 * {@code long} or a {@code double}. Both mean the same thing to this decoder - drop the top of the
 * modelled stack - because a value of either width occupies exactly one entry on it. That is the
 * whole reason the two opcodes share an arm in {@code BytecodeParser.stack}, and it is worth a test
 * because the JVM specification invites the opposite conclusion: {@code POP2} also has a form that
 * discards two single-width values, and handling that form would take two entries off this stack
 * and corrupt it.
 *
 * <p>Only the wide form arises. javac tracks the width of each item and reaches for the wide drop
 * only for a {@code long} or a {@code double}; every {@code pop2} in this project's compiled classes
 * is one of the two below.
 *
 * <p>{@link #discardsAnInlinedConstantsQualifier} is the shape that brought this up. A
 * {@code final} field of primitive type with a constant initialiser is a constant variable
 * (JLS 4.12.4), so the compiler substitutes its value at the use site - but still evaluates the
 * {@code this} in front of it and discards the result.
 */
class Test_5070_DiscardedValues {

    public record Sold(int objKey, String name) {
    }

    static long aLong() {
        return 1;
    }

    static double aDouble() {
        return 1;
    }

    static int anInt() {
        return 1;
    }

    /** POP2. Threw {@code EmptyStackException} while the arm took two values off the stack. */
    @Test
    void discardsALongValuedCall() {
        var db = new CapturingDatabase();

        db.from(Sold.class).where(s -> {
            aLong();
            return s.objKey() > 7;
        }).list();

        assertEquals("SELECT Sold.* FROM Sold WHERE Sold.objKey > ?", db.sql());
        assertEquals(List.of(7), db.params());
    }

    /** POP2 again: a double is the same width as a long. */
    @Test
    void discardsADoubleValuedCall() {
        var db = new CapturingDatabase();

        db.from(Sold.class).where(s -> {
            aDouble();
            return s.objKey() > 7;
        }).list();

        assertEquals("SELECT Sold.* FROM Sold WHERE Sold.objKey > ?", db.sql());
        assertEquals(List.of(7), db.params());
    }

    /** Plain POP, as the control: a single-width value discarded the ordinary way. */
    @Test
    void discardsAnIntValuedCall() {
        var db = new CapturingDatabase();

        db.from(Sold.class).where(s -> {
            anInt();
            return s.objKey() > 7;
        }).list();

        assertEquals("SELECT Sold.* FROM Sold WHERE Sold.objKey > ?", db.sql());
        assertEquals(List.of(7), db.params());
    }

    private final int constantVariable = 7;

    /** Plain POP, discarding the {@code this} in front of a constant the compiler has inlined. */
    @Test
    void discardsAnInlinedConstantsQualifier() {
        var db = new CapturingDatabase();

        db.from(Sold.class).where(s -> s.objKey() > this.constantVariable).list();

        assertEquals("SELECT Sold.* FROM Sold WHERE Sold.objKey > ?", db.sql());
        assertEquals(List.of(7), db.params());
    }
}
