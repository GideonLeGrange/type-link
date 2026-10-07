package rec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalTime;

/** The newer column types: time of day, exact numbers, and the small integer and character types. */
public record Ledger(Long id, LocalTime opens, BigDecimal amount, BigInteger serial, short copies, byte level,
                     char initial, Character grade) {
}
