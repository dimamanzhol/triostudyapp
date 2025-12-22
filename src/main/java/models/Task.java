package models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Task {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
    private TaskType taskType;

    // NEW: Time tracking properties
    private final BooleanProperty active = new SimpleBooleanProperty(false);
    private final IntegerProperty totalTimeSpent = new SimpleIntegerProperty(0);
    private final IntegerProperty estimatedTime = new SimpleIntegerProperty(0);

    public Task() {
        this(TaskType.WORK);
    }

    public Task(TaskType taskType) {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.completed = false;
        this.taskType = taskType;
    }

    public Task(String title, String description) {
        this(title, description, 0, TaskType.WORK);
    }

    public Task(String title, String description, TaskType taskType) {
        this(title, description, 0, taskType);
    }

    public Task(String title, String description, int estimatedMinutes) {
        this(title, description, estimatedMinutes, TaskType.WORK);
    }

    public Task(String title, String description, int estimatedMinutes, TaskType taskType) {
        this(taskType);
        this.title = title;
        this.description = description;
        this.estimatedTime.set(estimatedMinutes);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void toggleCompleted() {
        this.completed = !this.completed;
    }

    // NEW: Time tracking methods
    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public int getTotalTimeSpent() {
        return totalTimeSpent.get();
    }

    public IntegerProperty totalTimeSpentProperty() {
        return totalTimeSpent;
    }

    public void setTotalTimeSpent(int totalTimeSpent) {
        this.totalTimeSpent.set(totalTimeSpent);
    }

    public void addTimeSpent(int minutes) {
        this.totalTimeSpent.set(this.totalTimeSpent.get() + minutes);
    }

    public int getEstimatedTime() {
        return estimatedTime.get();
    }

    public IntegerProperty estimatedTimeProperty() {
        return estimatedTime;
    }

    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime.set(estimatedTime);
    }

    // Helper methods
    public int getTimeRemaining() {
        return Math.max(0, estimatedTime.get() - totalTimeSpent.get());
    }

    public boolean isOverEstimate() {
        return estimatedTime.get() > 0 && totalTimeSpent.get() > estimatedTime.get();
    }

    public double getProgressPercentage() {
        if (estimatedTime.get() <= 0) return 0.0;
        return Math.min(1.0, (double) totalTimeSpent.get() / estimatedTime.get());
    }

    public String getTimeSpentFormatted() {
        int minutes = totalTimeSpent.get();
        if (minutes < 60) {
            return minutes + "m";
        } else {
            int hours = minutes / 60;
            int mins = minutes % 60;
            return hours + "h " + mins + "m";
        }
    }

    public String getTimeRemainingFormatted() {
        int minutes = getTimeRemaining();
        if (minutes < 60) {
            return minutes + "m left";
        } else {
            int hours = minutes / 60;
            int mins = minutes % 60;
            return hours + "h " + mins + "m left";
        }
    }

    @Override
    public String toString() {
        String timeInfo = "";
        if (estimatedTime.get() > 0) {
            timeInfo = String.format(" (%d/%dm)", totalTimeSpent.get(), estimatedTime.get());
        } else if (totalTimeSpent.get() > 0) {
            timeInfo = String.format(" (%dm)", totalTimeSpent.get());
        }
        return title + (completed ? " ✓" : "") + timeInfo;
    }
}