package bean;

import java.time.LocalDateTime;
import java.util.Objects;

public class Meeting extends Bean {
    private String subject;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Meeting() {
        super();
    }

    public Meeting(Long id, String subject, LocalDateTime startTime, LocalDateTime endTime) {
        super(id);
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Meeting meeting = (Meeting) o;
        return Objects.equals(subject, meeting.subject) &&
               Objects.equals(startTime, meeting.startTime) &&
               Objects.equals(endTime, meeting.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subject, startTime, endTime);
    }
}
