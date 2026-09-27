package decoding;

import me.legrange.typelink.lambda.DecoderException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@code instanceof} should be refused, not silently decoded into the wrong thing.
 *
 * <p><b>The bug this targets.</b> {@code BytecodeParser}'s main dispatch treats every
 * {@code TypeCheckInstruction} - both {@code CHECKCAST} and {@code INSTANCEOF} - as a no-op:
 * {@code case TypeCheckInstruction _: break;}. That is right for {@code CHECKCAST}, which the
 * compiler inserts around unboxing and generic casts and which leaves the stack exactly as it found
 * it. It is wrong for {@code INSTANCEOF}, which replaces the reference on the stack with a boolean -
 * skipping it leaves the original object reference sitting where the boolean result should be. Today
 * that silently compiles {@code t.value() instanceof String} into
 * {@code WHERE Thing.value <> false} - comparing an arbitrary column to a boolean literal, which is
 * not what the source lambda said and produces no error anywhere validate-queries could catch.
 *
 * <p>A type check has no general SQL translation for an arbitrary column - there is no standard way
 * to ask a database "is the value stored here a {@code java.lang.String}" without a discriminator
 * column designed for that - so refusal, not translation, is the fix this test targets: a clear
 * {@link DecoderException} rather than silent, wrong SQL.
 *
 * <p><b>This test currently fails</b>: today nothing is thrown at all. It is written against the fix,
 * not against today's silent, wrong behaviour.
 */
class Test_6050_InstanceOfIsSilentlyWrong {

    private record Thing(Long id, Object value) {
    }

    @Test
    void instanceOfIsRefusedRatherThanSilentlyMisdecoded() {
        var db = new CapturingDatabase();

        assertThrows(DecoderException.class,
                () -> db.from(Thing.class).where(t -> t.value() instanceof String).list(),
                "instanceof cannot be translated to SQL for an arbitrary column and must not be "
                        + "silently dropped");
    }
}
