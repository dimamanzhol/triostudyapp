package controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.Task;
import models.StudySession;
import services.DataManager;
import services.TimerService;

public class MainController {
    private final DataManager dataManager;
    private final TimerService timerService;

    private VBox mainView;
    private VBox timerSection;
    private VBox tasksSection;

    private Label timeLabel;
    private Label sessionTypeLabel;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;

    private ListView<Task> taskListView;
    private TextField taskTitleField;
    private Button addTaskButton;

    private Label statsLabel;

    public MainController(DataManager dataManager) {
        this.dataManager = dataManager;
        this.timerService = new TimerService();
        setupTimerService();
        createView();
        updateStats();
    }

    private void setupTimerService() {
        timerService.setOnSessionComplete(() -> {
            if (!timerService.isBreakProperty().get()) {
                StudySession session = new StudySession(
                    timerService.getWorkDurationMinutes(),
                    "Pomodoro Session"
                );
                dataManager.addStudySession(session);
                timerService.switchToBreak();
                showAlert("Work session complete! Time for a break.");
            } else {
                timerService.switchToWork();
                showAlert("Break complete! Ready for another work session?");
            }
            updateStats();
        });
    }

    private void createView() {
        mainView = new VBox(20);
        mainView.setPadding(new Insets(20));
        mainView.getStyleClass().add("main-container");

        createTimerSection();
        createTasksSection();

        mainView.getChildren().addAll(timerSection, tasksSection);
    }

    private void createTimerSection() {
        timerSection = new VBox(15);
        timerSection.setAlignment(Pos.CENTER);
        timerSection.getStyleClass().add("timer-section");

        timeLabel = new Label();
        timeLabel.textProperty().bind(timerService.timeDisplayProperty());
        timeLabel.getStyleClass().add("timer-display");

        sessionTypeLabel = new Label("Work Session");
        sessionTypeLabel.getStyleClass().add("session-type");

        timerService.isBreakProperty().addListener((obs, oldVal, newVal) -> {
            sessionTypeLabel.setText(newVal ? "Break Time" : "Work Session");
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        startButton = new Button("Start");
        startButton.setOnAction(e -> timerService.start());
        startButton.getStyleClass().addAll("timer-button", "start-button");

        pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> timerService.pause());
        pauseButton.getStyleClass().addAll("timer-button", "pause-button");

        resetButton = new Button("Reset");
        resetButton.setOnAction(e -> timerService.reset());
        resetButton.getStyleClass().addAll("timer-button", "reset-button");

        buttonBox.getChildren().addAll(startButton, pauseButton, resetButton);

        statsLabel = new Label();
        statsLabel.getStyleClass().add("stats-label");

        timerSection.getChildren().addAll(sessionTypeLabel, timeLabel, buttonBox, statsLabel);
    }

    private void createTasksSection() {
        tasksSection = new VBox(10);
        tasksSection.getStyleClass().add("tasks-section");

        Label tasksTitle = new Label("Tasks");
        tasksTitle.getStyleClass().add("section-title");

        HBox addTaskBox = new HBox(10);
        addTaskBox.setAlignment(Pos.CENTER_LEFT);

        taskTitleField = new TextField();
        taskTitleField.setPromptText("Enter new task...");
        taskTitleField.setPrefWidth(250);

        addTaskButton = new Button("Add Task");
        addTaskButton.setOnAction(e -> addTask());
        addTaskButton.getStyleClass().add("add-button");

        addTaskBox.getChildren().addAll(taskTitleField, addTaskButton);

        taskListView = new ListView<>();
        taskListView.setPrefHeight(200);
        taskListView.setCellFactory(param -> new TaskListCell());

        loadTasks();

        tasksSection.getChildren().addAll(tasksTitle, addTaskBox, taskListView);
    }

    private void addTask() {
        String title = taskTitleField.getText().trim();
        if (!title.isEmpty()) {
            Task task = new Task(title, "");
            dataManager.addTask(task);
            taskTitleField.clear();
            loadTasks();
        }
    }

    private void loadTasks() {
        taskListView.getItems().setAll(dataManager.getTasks());
    }

    private void updateStats() {
        int totalMinutesToday = dataManager.getTotalStudyTimeToday();
        int completedTasksCount = (int) dataManager.getTasks().stream()
                .filter(Task::isCompleted)
                .count();

        String statsText = String.format("Today: %d min study time | %d tasks completed",
                totalMinutesToday, completedTasksCount);
        statsLabel.setText(statsText);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Session Complete");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return mainView;
    }

    private class TaskListCell extends ListCell<Task> {
        private HBox content;
        private CheckBox checkBox;
        private Label titleLabel;
        private Button deleteButton;

        public TaskListCell() {
            super();
            checkBox = new CheckBox();
            titleLabel = new Label();
            deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-button");

            checkBox.setOnAction(e -> {
                Task task = getItem();
                if (task != null) {
                    task.setCompleted(checkBox.isSelected());
                    dataManager.updateTask(task);
                    updateStats();
                }
            });

            deleteButton.setOnAction(e -> {
                Task task = getItem();
                if (task != null) {
                    dataManager.removeTask(task);
                    loadTasks();
                    updateStats();
                }
            });

            content = new HBox(10, checkBox, titleLabel, deleteButton);
            content.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
        }

        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);

            if (empty || task == null) {
                setGraphic(null);
            } else {
                checkBox.setSelected(task.isCompleted());
                titleLabel.setText(task.getTitle());
                titleLabel.getStyleClass().setAll("task-title");
                if (task.isCompleted()) {
                    titleLabel.getStyleClass().add("completed");
                }
                setGraphic(content);
            }
        }
    }
}