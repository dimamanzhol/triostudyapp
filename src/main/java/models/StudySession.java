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
    private SessionType sessionType; // NEW: Callout type (WORK or STUDY)
    private String projectName; // NEW: Project or category name

    public StudySession() {
        this.id = java.util.UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
        this.sessionType = SessionType.WORK; // Default to WORK
    }

    public StudySession(int durationMinutes, String subject) {
        this();
        this.durationMinutes = durationMinutes;
        this.subject = subject;
        this.endTime = startTime.plusMinutes(durationMinutes);
    }

    public StudySession(int durationMinutes, String subject, SessionType sessionType) {
        this();
        this.durationMinutes = durationMinutes;
        this.subject = subject;
        this.sessionType = sessionType;
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

    public StudySession(LocalDateTime startTime, LocalDateTime endTime, String subject, String notes, SessionType sessionType) {
        this();
        this.startTime = startTime;
        this.endTime = endTime;
        this.subject = subject;
        this.notes = notes;
        this.sessionType = sessionType;
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

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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
        String sessionTypeLabel = sessionType != null ? sessionType.getFullLabel() : "Work";
        String subjectLabel = subject != null ? subject : (sessionType == SessionType.WORK ? "Work Session" : "Study Session");
        String projectLabel = projectName != null && !projectName.isEmpty() ? " [" + projectName + "]" : "";

        return String.format("%s - %s%s (%s)",
            getFormattedDate(),
            sessionTypeLabel + " " + subjectLabel,
            projectLabel,
            getDurationFormatted());
    }
}