package services;

import models.StudySession;
import models.Task;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataManager {
    private static final String DATA_DIR = "data";
    private static final String TASKS_FILE = DATA_DIR + "/tasks.json";
    private static final String SESSIONS_FILE = DATA_DIR + "/sessions.json";

    private List<Task> tasks;
    private List<StudySession> studySessions;

    public DataManager() {
        this.tasks = new ArrayList<>();
        this.studySessions = new ArrayList<>();
        createDataDirectory();
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
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
        File file = new File(TASKS_FILE);
        if (!file.exists()) {
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(TASKS_FILE)));
            JSONArray jsonArray = new JSONArray(content);

            tasks.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTask = jsonArray.getJSONObject(i);
                Task task = new Task();
                task.setId(jsonTask.getString("id"));
                task.setTitle(jsonTask.getString("title"));
                task.setDescription(jsonTask.optString("description", ""));
                task.setCompleted(jsonTask.getBoolean("completed"));
                task.setCreatedAt(LocalDateTime.parse(jsonTask.getString("createdAt")));
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
            jsonArray.put(jsonTask);
        }

        try {
            Files.write(Paths.get(TASKS_FILE), jsonArray.toString(2).getBytes());
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }

    private void loadStudySessions() {
        File file = new File(SESSIONS_FILE);
        if (!file.exists()) {
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(SESSIONS_FILE)));
            JSONArray jsonArray = new JSONArray(content);

            studySessions.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonSession = jsonArray.getJSONObject(i);
                StudySession session = new StudySession();
                session.setId(jsonSession.getString("id"));
                session.setStartTime(LocalDateTime.parse(jsonSession.getString("startTime")));
                session.setEndTime(LocalDateTime.parse(jsonSession.getString("endTime")));
                session.setSubject(jsonSession.optString("subject", ""));
                session.setNotes(jsonSession.optString("notes", ""));
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
            jsonArray.put(jsonSession);
        }

        try {
            Files.write(Paths.get(SESSIONS_FILE), jsonArray.toString(2).getBytes());
        } catch (IOException e) {
            System.err.println("Error saving study sessions: " + e.getMessage());
        }
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
}