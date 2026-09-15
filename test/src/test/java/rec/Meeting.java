package rec;

import java.time.LocalDateTime;

public record Meeting(Long id, String subject, LocalDateTime startTime, LocalDateTime endTime) {
}
