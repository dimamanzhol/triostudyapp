package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StudySession {
    private String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;
    private String subject;
    private String notes;

    public StudySession() {
        this.id = java.util.UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
    }

    public StudySession(int durationMinutes, String subject) {
        this();
        this.durationMinutes = durationMinutes;
        this.subject = subject;
        this.endTime = startTime.plusMinutes(durationMinutes);
    }

    public StudySession(LocalDateTime startTime, LocalDateTime endTime, String subject, String notes) {
        this();
        this.startTime = startTime;
        this.endTime = endTime;
        this.subject = subject;
        this.notes = notes;
        this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return startTime.format(formatter);
    }

    public String getDurationFormatted() {
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)",
            getFormattedDate(),
            subject != null ? subject : "Study Session",
            getDurationFormatted());
    }
}