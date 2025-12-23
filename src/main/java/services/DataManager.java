package services;

import models.StudySession;
import models.Task;
import models.SessionType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataManager {
    private static final String TASKS_FILE = "tasks.json";
    private static final String SESSIONS_FILE = "sessions.json";
    private static final String THEME_FILE = "theme.json";

    private List<Task> tasks;
    private List<StudySession> studySessions;
    private LocalStorage localStorage;

    public DataManager() {
        this.tasks = new ArrayList<>();
        this.studySessions = new ArrayList<>();
        this.localStorage = new LocalStorage();
        this.localStorage.ensureDirectoriesExist();
    }

    public void loadData() {
        loadTasks();
        loadStudySessions();
    }

    public void saveData() {
        saveTasks();
        saveStudySessions();
    }

    private void loadTasks() {
        try {
            JSONObject data = localStorage.readJsonFile(TASKS_FILE);
            JSONArray jsonArray = data.optJSONArray("tasks");
            if (jsonArray == null) {
                // Try reading as array directly (legacy format)
                jsonArray = new JSONArray(data.toString());
                if (jsonArray.length() == 0) {
                    return;
                }
            }

            tasks.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTask = jsonArray.getJSONObject(i);
                Task task = new Task();
                task.setId(jsonTask.getString("id"));
                task.setTitle(jsonTask.getString("title"));
                task.setDescription(jsonTask.optString("description", ""));
                task.setCompleted(jsonTask.getBoolean("completed"));
                task.setCreatedAt(LocalDateTime.parse(jsonTask.getString("createdAt")));

                // Handle time tracking properties
                task.setActive(jsonTask.optBoolean("active", false));
                task.setTotalTimeSpent(jsonTask.optInt("totalTimeSpent", 0));
                task.setEstimatedTime(jsonTask.optInt("estimatedTime", 0));

                tasks.add(task);
            }
        } catch (Exception e) {
            System.err.println("Error loading tasks: " + e.getMessage());
        }
    }

    private void saveTasks() {
        JSONArray jsonArray = new JSONArray();
        for (Task task : tasks) {
            JSONObject jsonTask = new JSONObject();
            jsonTask.put("id", task.getId());
            jsonTask.put("title", task.getTitle());
            jsonTask.put("description", task.getDescription());
            jsonTask.put("completed", task.isCompleted());
            jsonTask.put("createdAt", task.getCreatedAt().toString());

            // Save time tracking properties
            jsonTask.put("active", task.isActive());
            jsonTask.put("totalTimeSpent", task.getTotalTimeSpent());
            jsonTask.put("estimatedTime", task.getEstimatedTime());

            jsonArray.put(jsonTask);
        }

        JSONObject data = new JSONObject();
        data.put("tasks", jsonArray);
        localStorage.writeJsonFile(TASKS_FILE, data);
    }

    private void loadStudySessions() {
        try {
            JSONObject data = localStorage.readJsonFile(SESSIONS_FILE);
            JSONArray jsonArray = data.optJSONArray("sessions");
            if (jsonArray == null) {
                // Try reading as array directly (legacy format)
                jsonArray = new JSONArray(data.toString());
                if (jsonArray.length() == 0) {
                    return;
                }
            }

            studySessions.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonSession = jsonArray.getJSONObject(i);
                StudySession session = new StudySession();
                session.setId(jsonSession.getString("id"));
                session.setStartTime(LocalDateTime.parse(jsonSession.getString("startTime")));
                session.setEndTime(LocalDateTime.parse(jsonSession.getString("endTime")));
                session.setSubject(jsonSession.optString("subject", ""));
                session.setNotes(jsonSession.optString("notes", ""));

                // Handle session type and project name (new fields)
                String sessionTypeStr = jsonSession.optString("sessionType", "WORK");
                try {
                    session.setSessionType(SessionType.valueOf(sessionTypeStr));
                } catch (IllegalArgumentException e) {
                    session.setSessionType(SessionType.WORK); // Default fallback
                }

                session.setProjectName(jsonSession.optString("projectName", ""));

                studySessions.add(session);
            }
        } catch (Exception e) {
            System.err.println("Error loading study sessions: " + e.getMessage());
        }
    }

    private void saveStudySessions() {
        JSONArray jsonArray = new JSONArray();
        for (StudySession session : studySessions) {
            JSONObject jsonSession = new JSONObject();
            jsonSession.put("id", session.getId());
            jsonSession.put("startTime", session.getStartTime().toString());
            jsonSession.put("endTime", session.getEndTime().toString());
            jsonSession.put("subject", session.getSubject());
            jsonSession.put("notes", session.getNotes());

            // Save new fields for session type and project name
            if (session.getSessionType() != null) {
                jsonSession.put("sessionType", session.getSessionType().toString());
            }
            if (session.getProjectName() != null) {
                jsonSession.put("projectName", session.getProjectName());
            }

            jsonArray.put(jsonSession);
        }

        JSONObject data = new JSONObject();
        data.put("sessions", jsonArray);
        localStorage.writeJsonFile(SESSIONS_FILE, data);
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        saveTasks();
    }

    public void updateTask(Task task) {
        saveTasks();
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    public void addStudySession(StudySession session) {
        studySessions.add(session);
        saveStudySessions();
    }

    public void removeStudySession(StudySession session) {
        studySessions.remove(session);
        saveStudySessions();
    }

    public List<StudySession> getStudySessions() {
        return new ArrayList<>(studySessions);
    }

    public int getTotalStudyTimeToday() {
        LocalDate today = LocalDate.now();
        return studySessions.stream()
                .filter(session -> session.getStartTime().toLocalDate().equals(today))
                .mapToInt(StudySession::getDurationMinutes)
                .sum();
    }

    public int getTotalStudyTimeThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return studySessions.stream()
                .filter(session -> session.getStartTime().toLocalDate().isAfter(weekStart.minusDays(1)))
                .mapToInt(StudySession::getDurationMinutes)
                .sum();
    }

    public List<StudySession> getStudySessionsForDate(LocalDate date) {
        return studySessions.stream()
                .filter(session -> session.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Task> getCompletedTasks() {
        return tasks.stream()
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
    }

    public List<Task> getActiveTasks() {
        return tasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.toList());
    }

    // ==================== EXPORT/IMPORT FUNCTIONALITY ====================

    /**
     * Export all data to a timestamped JSON file
     * @return Path to the exported file, or null if export failed
     */
    public String exportAllData() {
        // Prepare data map
        Map<String, JSONObject> dataMap = new HashMap<>();

        // Export tasks
        JSONObject tasksData = new JSONObject();
        JSONArray tasksArray = new JSONArray();
        for (Task task : tasks) {
            JSONObject jsonTask = new JSONObject();
            jsonTask.put("id", task.getId());
            jsonTask.put("title", task.getTitle());
            jsonTask.put("description", task.getDescription());
            jsonTask.put("completed", task.isCompleted());
            jsonTask.put("createdAt", task.getCreatedAt().toString());
            jsonTask.put("active", task.isActive());
            jsonTask.put("totalTimeSpent", task.getTotalTimeSpent());
            jsonTask.put("estimatedTime", task.getEstimatedTime());
            tasksArray.put(jsonTask);
        }
        tasksData.put("tasks", tasksArray);
        dataMap.put("tasks", tasksData);

        // Export sessions
        JSONObject sessionsData = new JSONObject();
        JSONArray sessionsArray = new JSONArray();
        for (StudySession session : studySessions) {
            JSONObject jsonSession = new JSONObject();
            jsonSession.put("id", session.getId());
            jsonSession.put("startTime", session.getStartTime().toString());
            jsonSession.put("endTime", session.getEndTime().toString());
            jsonSession.put("subject", session.getSubject());
            jsonSession.put("notes", session.getNotes());
            if (session.getSessionType() != null) {
                jsonSession.put("sessionType", session.getSessionType().toString());
            }
            if (session.getProjectName() != null) {
                jsonSession.put("projectName", session.getProjectName());
            }
            sessionsArray.put(jsonSession);
        }
        sessionsData.put("sessions", sessionsArray);
        dataMap.put("sessions", sessionsData);

        // Export theme
        JSONObject themeData = new JSONObject();
        try {
            java.nio.file.Path themeFile = java.nio.file.Paths.get("data", THEME_FILE);
            if (java.nio.file.Files.exists(themeFile)) {
                String content = new String(java.nio.file.Files.readAllBytes(themeFile));
                themeData = new JSONObject(content);
            }
        } catch (Exception e) {
            System.err.println("Could not read theme data: " + e.getMessage());
        }
        dataMap.put("theme", themeData);

        return localStorage.exportData(dataMap);
    }

    /**
     * Import data from an export file
     * @param exportFilePath Path to the export file
     * @return true if import was successful
     */
    public boolean importData(String exportFilePath) {
        try {
            Map<String, JSONObject> importedData = localStorage.importData(exportFilePath);
            if (importedData == null) {
                return false;
            }

            // Import tasks
            if (importedData.containsKey("tasks")) {
                JSONObject tasksData = importedData.get("tasks");
                JSONArray tasksArray = tasksData.optJSONArray("tasks");
                if (tasksArray != null) {
                    tasks.clear();
                    for (int i = 0; i < tasksArray.length(); i++) {
                        JSONObject jsonTask = tasksArray.getJSONObject(i);
                        Task task = new Task();
                        task.setId(jsonTask.getString("id"));
                        task.setTitle(jsonTask.getString("title"));
                        task.setDescription(jsonTask.optString("description", ""));
                        task.setCompleted(jsonTask.getBoolean("completed"));
                        task.setCreatedAt(LocalDateTime.parse(jsonTask.getString("createdAt")));
                        task.setActive(jsonTask.optBoolean("active", false));
                        task.setTotalTimeSpent(jsonTask.optInt("totalTimeSpent", 0));
                        task.setEstimatedTime(jsonTask.optInt("estimatedTime", 0));
                        tasks.add(task);
                    }
                }
            }

            // Import sessions
            if (importedData.containsKey("sessions")) {
                JSONObject sessionsData = importedData.get("sessions");
                JSONArray sessionsArray = sessionsData.optJSONArray("sessions");
                if (sessionsArray != null) {
                    studySessions.clear();
                    for (int i = 0; i < sessionsArray.length(); i++) {
                        JSONObject jsonSession = sessionsArray.getJSONObject(i);
                        StudySession session = new StudySession();
                        session.setId(jsonSession.getString("id"));
                        session.setStartTime(LocalDateTime.parse(jsonSession.getString("startTime")));
                        session.setEndTime(LocalDateTime.parse(jsonSession.getString("endTime")));
                        session.setSubject(jsonSession.optString("subject", ""));
                        session.setNotes(jsonSession.optString("notes", ""));

                        String sessionTypeStr = jsonSession.optString("sessionType", "WORK");
                        try {
                            session.setSessionType(SessionType.valueOf(sessionTypeStr));
                        } catch (IllegalArgumentException e) {
                            session.setSessionType(SessionType.WORK);
                        }

                        session.setProjectName(jsonSession.optString("projectName", ""));
                        studySessions.add(session);
                    }
                }
            }

            // Import theme
            if (importedData.containsKey("theme")) {
                JSONObject themeData = importedData.get("theme");
                localStorage.writeJsonFile(THEME_FILE, themeData);
            }

            // Save all imported data
            saveData();
            return true;

        } catch (Exception e) {
            System.err.println("Error importing data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get list of available export files
     */
    public File[] getExportFiles() {
        return localStorage.getExportFiles();
    }

    /**
     * Get list of available backup files for a specific data file
     */
    public File[] getBackupFiles(String filename) {
        return localStorage.getBackupFiles(filename);
    }

    /**
     * Restore data from a backup file
     */
    public boolean restoreFromBackup(String backupFilename, String targetFilename) {
        boolean success = localStorage.restoreFromBackup(backupFilename, targetFilename);
        if (success) {
            loadData();
        }
        return success;
    }

    /**
     * Get storage statistics
     */
    public JSONObject getStorageStats() {
        return localStorage.getStorageStats();
    }

    /**
     * Clear all data (use with caution)
     */
    public void clearAllData() {
        tasks.clear();
        studySessions.clear();
        localStorage.clearAllData();
    }
}