package rec;

import java.util.Date;

public record Appointment(Long id, String subject, Date startTime) {
}
