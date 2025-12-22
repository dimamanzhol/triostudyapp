package models;

public enum TaskType {
    WORK("Work", "💼", "Professional tasks, projects, meetings"),
    STUDY("Study", "📚", "Learning, homework, exam preparation");

    private final String displayName;
    private final String icon;
    private String description;

    TaskType(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getFullLabel() {
        return icon + " " + displayName;
    }

    public String getTaskPrompt() {
        switch (this) {
            case WORK:
                return "Add work task...";
            case STUDY:
                return "Add study task...";
            default:
                return "Add task...";
        }
    }

    public String getListTitle() {
        return displayName + " Tasks";
    }

    public String getColorCode() {
        switch (this) {
            case WORK:
                return "#2196F3"; // Blue
            case STUDY:
                return "#4CAF50"; // Green
            default:
                return "#666666";
        }
    }

    public String getLightColorCode() {
        switch (this) {
            case WORK:
                return "#E3F2FD"; // Light blue
            case STUDY:
                return "#E8F5E8"; // Light green
            default:
                return "#f5f5f5";
        }
    }
}