package models;

public class SessionConfiguration {
    private SessionType sessionType;
    private int workDurationMinutes;
    private int breakDurationMinutes;
    private boolean autoStartBreaks;
    private boolean autoStartWork;
    private String defaultProjectName;

    public SessionConfiguration(SessionType sessionType) {
        this.sessionType = sessionType;
        this.workDurationMinutes = sessionType.getDefaultWorkDuration();
        this.breakDurationMinutes = sessionType.getDefaultBreakDuration();
        this.autoStartBreaks = true;
        this.autoStartWork = true;
        this.defaultProjectName = "";
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public int getWorkDurationMinutes() {
        return workDurationMinutes;
    }

    public void setWorkDurationMinutes(int workDurationMinutes) {
        this.workDurationMinutes = Math.max(1, Math.min(180, workDurationMinutes)); // 1-180 minutes range
    }

    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    public void setBreakDurationMinutes(int breakDurationMinutes) {
        this.breakDurationMinutes = Math.max(1, Math.min(60, breakDurationMinutes)); // 1-60 minutes range
    }

    public boolean isAutoStartBreaks() {
        return autoStartBreaks;
    }

    public void setAutoStartBreaks(boolean autoStartBreaks) {
        this.autoStartBreaks = autoStartBreaks;
    }

    public boolean isAutoStartWork() {
        return autoStartWork;
    }

    public void setAutoStartWork(boolean autoStartWork) {
        this.autoStartWork = autoStartWork;
    }

    public String getDefaultProjectName() {
        return defaultProjectName;
    }

    public void setDefaultProjectName(String defaultProjectName) {
        this.defaultProjectName = defaultProjectName;
    }

    public void resetToDefaults() {
        this.workDurationMinutes = sessionType.getDefaultWorkDuration();
        this.breakDurationMinutes = sessionType.getDefaultBreakDuration();
        this.autoStartBreaks = true;
        this.autoStartWork = true;
    }

    @Override
    public String toString() {
        return String.format("%s Configuration: %d min work, %d min break",
            sessionType.getDisplayName(), workDurationMinutes, breakDurationMinutes);
    }
}