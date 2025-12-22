package models;

public enum SessionType {
    WORK("Work", "💼", "Deep work, meetings, project development"),
    STUDY("Study", "📚", "Learning, research, exam preparation");

    private final String displayName;
    private final String icon;
    private final String description;

    SessionType(String displayName, String icon, String description) {
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

    // Default timer configurations
    public int getDefaultWorkDuration() {
        switch (this) {
            case WORK:
                return 25; // 25 minutes for work
            case STUDY:
                return 45; // 45 minutes for study
            default:
                return 25;
        }
    }

    public int getDefaultBreakDuration() {
        switch (this) {
            case WORK:
                return 5; // 5 minutes break for work
            case STUDY:
                return 10; // 10 minutes break for study
            default:
                return 5;
        }
    }

    public String getSessionCompleteMessage() {
        switch (this) {
            case WORK:
                return "Work session complete! Time for a quick break.";
            case STUDY:
                return "Study session complete! Take a longer break to recharge.";
            default:
                return "Session complete! Time for a break.";
        }
    }

    public String getBreakCompleteMessage() {
        switch (this) {
            case WORK:
                return "Break complete! Ready for another focused work session?";
            case STUDY:
                return "Break complete! Ready to continue studying?";
            default:
                return "Break complete! Ready for another session?";
        }
    }
}