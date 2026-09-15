package rec;

import java.time.LocalDate;

public record Person(Long id, Long clientId, String name, int age, String email, Sex sex, LocalDate birthDay) {
    public enum Sex {
        MALE, FEMALE
    }
}
