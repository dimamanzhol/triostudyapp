package controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.Task;
import models.StudySession;
import services.DataManager;
import services.TimerService;
import org.json.JSONObject;

import java.util.List;

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
    private Button themeToggleButton; // NEW: Theme toggle button

    private ListView<Task> taskListView;
    private TextField taskTitleField;
    private TextField estimatedTimeField;
    private Button addTaskButton;
    private ComboBox<String> filterComboBox;
    private ComboBox<String> sortComboBox;

    private Label statsLabel;
    private Label currentTaskLabel; // NEW: Shows which task is currently active
    private Label taskStatsLabel; // NEW: Task statistics

    private Task activeTask = null; // NEW: Currently focused task
    private boolean isDarkMode = false; // NEW: Theme state

    public MainController(DataManager dataManager) {
        this.dataManager = dataManager;
        this.timerService = new TimerService();
        loadThemePreference(); // NEW: Load theme preference
        setupTimerService();
        createView();
        updateStats();
    }

    private void setupTimerService() {
        timerService.setOnSessionComplete(() -> {
            if (!timerService.isBreakProperty().get()) {
                // Add time to active task
                if (activeTask != null) {
                    activeTask.addTimeSpent(timerService.getWorkDurationMinutes());

                    // Check if task should be marked as completed
                    checkTaskCompletion();

                    // Update task list display
                    taskListView.refresh();
                }

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

        // Apply theme after all components are created
        applyTheme(); // NEW: Apply initial theme
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

        // NEW: Current task label
        currentTaskLabel = new Label("No task selected");
        currentTaskLabel.getStyleClass().add("current-task-label");

        // NEW: Theme toggle button at the top
        HBox topControlsBox = new HBox();
        topControlsBox.setAlignment(Pos.TOP_RIGHT);
        themeToggleButton = new Button("🌙");
        themeToggleButton.setOnAction(e -> toggleTheme());
        themeToggleButton.getStyleClass().add("theme-toggle");
        topControlsBox.getChildren().add(themeToggleButton);

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

        timerSection.getChildren().addAll(topControlsBox, sessionTypeLabel, timeLabel, currentTaskLabel, buttonBox, statsLabel);
    }

    private void createTasksSection() {
        tasksSection = new VBox(10);
        tasksSection.getStyleClass().add("tasks-section");

        // Header with title and filter/sort controls
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(15);

        Label tasksTitle = new Label("Tasks");
        tasksTitle.getStyleClass().add("section-title");

        // Filter dropdown
        filterComboBox = new ComboBox<>();
        filterComboBox.getItems().addAll("All Tasks", "Active", "Completed");
        filterComboBox.setValue("All Tasks");
        filterComboBox.getStyleClass().add("filter-combo");
        filterComboBox.setOnAction(e -> applyFiltersAndSort());

        // Sort dropdown
        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Newest First", "Oldest First", "Name A-Z", "Name Z-A", "Time Spent", "Progress");
        sortComboBox.setValue("Newest First");
        sortComboBox.getStyleClass().add("sort-combo");
        sortComboBox.setOnAction(e -> applyFiltersAndSort());

        headerBox.getChildren().addAll(tasksTitle, filterComboBox, sortComboBox);

        // Enhanced add task box with time estimation
        VBox addTaskContainer = new VBox(8);
        addTaskContainer.getStyleClass().add("add-task-container");

        HBox addTaskBox = new HBox(10);
        addTaskBox.setAlignment(Pos.CENTER_LEFT);

        taskTitleField = new TextField();
        taskTitleField.setPromptText("Enter new task...");
        taskTitleField.setPrefWidth(280);

        estimatedTimeField = new TextField();
        estimatedTimeField.setPromptText("Est. min");
        estimatedTimeField.setPrefWidth(80);

        addTaskButton = new Button("Add Task");
        addTaskButton.setOnAction(e -> addTask());
        addTaskButton.getStyleClass().add("add-button");

        addTaskBox.getChildren().addAll(taskTitleField, estimatedTimeField, addTaskButton);

        // Task statistics label
        taskStatsLabel = new Label();
        taskStatsLabel.getStyleClass().add("task-stats-label");
        updateTaskStats();

        addTaskContainer.getChildren().addAll(addTaskBox, taskStatsLabel);

        taskListView = new ListView<>();
        taskListView.setPrefHeight(300);
        taskListView.setCellFactory(param -> new EnhancedTaskListCell());

        // Double-click to edit
        taskListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
                if (selectedTask != null) {
                    showEditTaskDialog(selectedTask);
                }
            }
        });

        loadTasks();

        tasksSection.getChildren().addAll(headerBox, addTaskContainer, taskListView);
    }

    private void addTask() {
        String title = taskTitleField.getText().trim();
        if (!title.isEmpty()) {
            // Parse estimated time
            int estimatedMinutes = 0;
            String estTimeText = estimatedTimeField.getText().trim();
            if (!estTimeText.isEmpty()) {
                try {
                    estimatedMinutes = Integer.parseInt(estTimeText);
                } catch (NumberFormatException e) {
                    // Invalid input, use 0
                }
            }

            Task task = new Task(title, "", estimatedMinutes);
            dataManager.addTask(task);
            taskTitleField.clear();
            estimatedTimeField.clear();
            loadTasks();
            updateTaskStats();
            updateStats();
        }
    }

    private void loadTasks() {
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        List<Task> tasks = new java.util.ArrayList<>(dataManager.getTasks());

        // Apply filter
        String filter = filterComboBox.getValue();
        if (filter != null) {
            switch (filter) {
                case "Active":
                    tasks.removeIf(Task::isCompleted);
                    break;
                case "Completed":
                    tasks.removeIf(task -> !task.isCompleted());
                    break;
            }
        }

        // Apply sort
        String sort = sortComboBox.getValue();
        if (sort != null) {
            switch (sort) {
                case "Newest First":
                    tasks.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                    break;
                case "Oldest First":
                    tasks.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
                    break;
                case "Name A-Z":
                    tasks.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
                    break;
                case "Name Z-A":
                    tasks.sort((a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle()));
                    break;
                case "Time Spent":
                    tasks.sort((a, b) -> Integer.compare(b.getTotalTimeSpent(), a.getTotalTimeSpent()));
                    break;
                case "Progress":
                    tasks.sort((a, b) -> Double.compare(b.getProgressPercentage(), a.getProgressPercentage()));
                    break;
            }
        }

        taskListView.getItems().setAll(tasks);
    }

    private void updateTaskStats() {
        int totalTasks = dataManager.getTasks().size();
        int activeTasks = (int) dataManager.getTasks().stream().filter(t -> !t.isCompleted()).count();
        int completedTasks = totalTasks - activeTasks;

        int totalEstimated = dataManager.getTasks().stream().mapToInt(Task::getEstimatedTime).sum();
        int totalSpent = dataManager.getTasks().stream().mapToInt(Task::getTotalTimeSpent).sum();

        String statsText = String.format("%d total | %d active | %d completed | %d/%d min spent",
                totalTasks, activeTasks, completedTasks, totalSpent, totalEstimated);
        taskStatsLabel.setText(statsText);
    }

    private void showEditTaskDialog(Task task) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit task: " + task.getTitle());

        // Set dialog button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, deleteButtonType, ButtonType.CANCEL);

        // Create form
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));

        TextField titleField = new TextField(task.getTitle());
        titleField.setPromptText("Task title");
        titleField.setPrefWidth(300);

        TextArea descArea = new TextArea(task.getDescription());
        descArea.setPromptText("Description (optional)");
        descArea.setPrefWidth(300);
        descArea.setPrefRowCount(3);

        TextField estTimeField = new TextField(String.valueOf(task.getEstimatedTime()));
        estTimeField.setPromptText("Estimated time (minutes)");

        form.getChildren().addAll(
                new Label("Title:"),
                titleField,
                new Label("Description:"),
                descArea,
                new Label("Estimated Time (minutes):"),
                estTimeField
        );

        dialog.getDialogPane().setContent(form);

        // Handle save
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String newTitle = titleField.getText().trim();
                if (!newTitle.isEmpty()) {
                    task.setTitle(newTitle);
                    task.setDescription(descArea.getText().trim());

                    try {
                        int estTime = Integer.parseInt(estTimeField.getText().trim());
                        task.setEstimatedTime(estTime);
                    } catch (NumberFormatException e) {
                        // Keep existing value
                    }

                    dataManager.updateTask(task);
                    loadTasks();
                    updateTaskStats();
                    taskListView.refresh();
                }
            } else if (dialogButton == deleteButtonType) {
                // Confirm deletion
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete Task");
                confirmAlert.setHeaderText("Delete \"" + task.getTitle() + "\"?");
                confirmAlert.setContentText("This action cannot be undone.");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        dataManager.removeTask(task);
                        loadTasks();
                        updateTaskStats();
                        updateStats();
                    }
                });
            }
            return null;
        });

        // Apply theme to dialog
        if (isDarkMode) {
            dialog.getDialogPane().getScene().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dark");
        }

        dialog.showAndWait();
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

    // NEW: Handle task focus when clicked
    private void handleTaskFocus(Task task) {
        // Deactivate previous task
        if (activeTask != null) {
            activeTask.setActive(false);
        }

        // Activate new task
        activeTask = task;
        task.setActive(true);

        // Update current task label
        String taskTimeInfo = "";
        if (task.getEstimatedTime() > 0) {
            taskTimeInfo = String.format(" (%d/%dm)", task.getTotalTimeSpent(), task.getEstimatedTime());
        } else if (task.getTotalTimeSpent() > 0) {
            taskTimeInfo = String.format(" (%dm)", task.getTotalTimeSpent());
        }

        currentTaskLabel.setText("Working on: " + task.getTitle() + taskTimeInfo);
        currentTaskLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        // Refresh task list to show active indicator
        taskListView.refresh();

        // Show subtle notification
        System.out.println("Focus set to: " + task.getTitle());
    }

    // NEW: Check if task should be marked as completed
    private void checkTaskCompletion() {
        if (activeTask == null || activeTask.isCompleted()) return;

        if (activeTask.getEstimatedTime() > 0 &&
            activeTask.getTotalTimeSpent() >= activeTask.getEstimatedTime()) {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Task Complete?");
            alert.setHeaderText("You've reached the estimated time for this task.");
            alert.setContentText("Mark \"" + activeTask.getTitle() + "\" as completed?");

            ButtonType yesButton = new ButtonType("Yes, mark complete", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No, keep working", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    activeTask.setCompleted(true);
                    activeTask.setActive(false);
                    activeTask = null;
                    currentTaskLabel.setText("No task selected");
                    currentTaskLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
                    taskListView.refresh();
                    updateStats();
                }
            });
        }
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

    // ==================== THEME MANAGEMENT ====================

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        saveThemePreference();
    }

    private void applyTheme() {
        // Only apply theme if the scene is available
        if (mainView.getScene() != null) {
            mainView.getScene().getStylesheets().clear();
            mainView.getScene().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        }

        if (isDarkMode) {
            // Add dark theme classes to all components
            mainView.getStyleClass().add("dark");
            timerSection.getStyleClass().add("dark");
            tasksSection.getStyleClass().add("dark");
            timeLabel.getStyleClass().add("dark");
            sessionTypeLabel.getStyleClass().add("dark");
            statsLabel.getStyleClass().add("dark");
            currentTaskLabel.getStyleClass().add("dark");
            startButton.getStyleClass().add("dark");
            pauseButton.getStyleClass().add("dark");
            resetButton.getStyleClass().add("dark");
            addTaskButton.getStyleClass().add("dark");
            taskTitleField.getStyleClass().add("dark");
            if (estimatedTimeField != null) estimatedTimeField.getStyleClass().add("dark");
            if (filterComboBox != null) filterComboBox.getStyleClass().add("dark");
            if (sortComboBox != null) sortComboBox.getStyleClass().add("dark");
            if (taskStatsLabel != null) taskStatsLabel.getStyleClass().add("dark");
            taskListView.getStyleClass().add("dark");
            if (themeToggleButton != null) {
                themeToggleButton.getStyleClass().add("dark");
                themeToggleButton.setText("☀️");
            }

            // Update task list cells
            if (taskListView != null) {
                taskListView.refresh();
            }
        } else {
            // Remove dark theme classes
            mainView.getStyleClass().remove("dark");
            timerSection.getStyleClass().remove("dark");
            tasksSection.getStyleClass().remove("dark");
            timeLabel.getStyleClass().remove("dark");
            sessionTypeLabel.getStyleClass().remove("dark");
            statsLabel.getStyleClass().remove("dark");
            currentTaskLabel.getStyleClass().remove("dark");
            startButton.getStyleClass().remove("dark");
            pauseButton.getStyleClass().remove("dark");
            resetButton.getStyleClass().remove("dark");
            addTaskButton.getStyleClass().remove("dark");
            taskTitleField.getStyleClass().remove("dark");
            if (estimatedTimeField != null) estimatedTimeField.getStyleClass().remove("dark");
            if (filterComboBox != null) filterComboBox.getStyleClass().remove("dark");
            if (sortComboBox != null) sortComboBox.getStyleClass().remove("dark");
            if (taskStatsLabel != null) taskStatsLabel.getStyleClass().remove("dark");
            taskListView.getStyleClass().remove("dark");
            if (themeToggleButton != null) {
                themeToggleButton.getStyleClass().remove("dark");
                themeToggleButton.setText("🌙");
            }

            // Update task list cells
            if (taskListView != null) {
                taskListView.refresh();
            }
        }
    }

    private void loadThemePreference() {
        try {
            java.nio.file.Path themeFile = java.nio.file.Paths.get("data", "theme.json");
            if (java.nio.file.Files.exists(themeFile)) {
                String content = new String(java.nio.file.Files.readAllBytes(themeFile));
                JSONObject themeData = new JSONObject(content);
                isDarkMode = themeData.optBoolean("darkMode", false);
            }
        } catch (Exception e) {
            System.err.println("Could not load theme preference: " + e.getMessage());
            isDarkMode = false;
        }
    }

    private void saveThemePreference() {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("data"));

            JSONObject themeData = new JSONObject();
            themeData.put("darkMode", isDarkMode);

            java.nio.file.Path themeFile = java.nio.file.Paths.get("data", "theme.json");
            java.nio.file.Files.write(themeFile, themeData.toString(2).getBytes());
        } catch (Exception e) {
            System.err.println("Could not save theme preference: " + e.getMessage());
        }
    }

    private class EnhancedTaskListCell extends ListCell<Task> {
        private HBox content;
        private CheckBox checkBox;
        private Label titleLabel;
        private Button deleteButton;
        private VBox rightContent;
        private Label timeLabel; // NEW: Shows time spent
        private Circle activeIndicator; // NEW: Shows if task is active

        public EnhancedTaskListCell() {
            super();
            checkBox = new CheckBox();
            titleLabel = new Label();
            deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-button");

            // NEW: Initialize time tracking components
            timeLabel = new Label();
            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
            activeIndicator = new Circle(4);
            activeIndicator.setFill(Color.TRANSPARENT);
            activeIndicator.setStroke(Color.web("#4CAF50"));
            activeIndicator.setStrokeWidth(2);

            // Create right side content (time label + delete button)
            rightContent = new VBox(2, timeLabel, deleteButton);
            rightContent.setAlignment(Pos.CENTER_RIGHT);

            // Create content with active indicator
            HBox taskContent = new HBox(8, activeIndicator, checkBox, titleLabel);
            taskContent.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            content = new HBox(10, taskContent, rightContent);
            content.setAlignment(Pos.CENTER_LEFT);
            content.getStyleClass().add("task-cell");
            HBox.setHgrow(taskContent, Priority.ALWAYS);

            // Enhanced checkbox with animations
            checkBox.setOnAction(e -> {
                Task task = getItem();
                if (task != null) {
                    boolean isNowCompleted = checkBox.isSelected();
                    task.setCompleted(isNowCompleted);
                    dataManager.updateTask(task);

                    // Play completion animation
                    if (isNowCompleted) {
                        playCompletionAnimation();
                    }

                    updateStats();
                }
            });

            // Add hover effect to the entire row
            content.setOnMouseEntered(this::onHoverEnter);
            content.setOnMouseExited(this::onHoverExit);

            // NEW: Click to focus on task
            content.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    Task task = getItem();
                    if (task != null && !task.isCompleted()) {
                        handleTaskFocus(task);
                    }
                }
            });

            deleteButton.setOnAction(e -> {
                Task task = getItem();
                if (task != null) {
                    // Play delete animation before removing
                    playDeleteAnimation(() -> {
                        dataManager.removeTask(task);
                        loadTasks();
                        updateStats();
                    });
                }
            });
        }

        private void playCompletionAnimation() {
            // Scale animation for checkbox
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), checkBox);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.3);
            scaleTransition.setToY(1.3);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(2);

            // Glow effect for completion
            Glow glow = new Glow();
            glow.setLevel(0.8);
            checkBox.setEffect(glow);

            // Fade animation for title
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), titleLabel);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.5);

            // Play animations
            ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
            parallelTransition.setOnFinished(e -> checkBox.setEffect(null));
            parallelTransition.play();
        }

        private void playDeleteAnimation(Runnable onComplete) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), content);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), content);
            scaleDown.setFromX(1.0);
            scaleDown.setFromY(1.0);
            scaleDown.setToX(0.8);
            scaleDown.setToY(0.8);

            ParallelTransition deleteTransition = new ParallelTransition(fadeOut, scaleDown);
            deleteTransition.setOnFinished(e -> onComplete.run());
            deleteTransition.play();
        }

        private void onHoverEnter(MouseEvent e) {
            Task task = getItem();
            if (task != null && !task.isCompleted()) {
                content.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 4;");
                deleteButton.setVisible(true);
            }
        }

        private void onHoverExit(MouseEvent e) {
            content.setStyle("-fx-background-color: transparent;");
            Task task = getItem();
            if (task != null && !task.isCompleted()) {
                deleteButton.setVisible(false);
            }
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

                // Apply dark theme classes if needed
                if (isDarkMode) {
                    titleLabel.getStyleClass().add("dark");
                    checkBox.getStyleClass().add("dark");
                    deleteButton.getStyleClass().add("dark");
                    content.getStyleClass().add("dark");
                    getStyleClass().add("dark");
                } else {
                    titleLabel.getStyleClass().remove("dark");
                    checkBox.getStyleClass().remove("dark");
                    deleteButton.getStyleClass().remove("dark");
                    content.getStyleClass().remove("dark");
                    getStyleClass().remove("dark");
                }

                // NEW: Update time label
                updateTimeLabel(task);

                // NEW: Update active indicator
                updateActiveIndicator(task);

                // Enhanced visual feedback for completed tasks
                if (task.isCompleted()) {
                    titleLabel.getStyleClass().add("completed");
                    content.getStyleClass().add("task-completed");
                    content.setStyle("-fx-opacity: 0.7;");
                    deleteButton.setVisible(false); // Hide delete for completed tasks
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");
                } else {
                    titleLabel.getStyleClass().remove("completed");
                    content.getStyleClass().remove("task-completed");
                    content.setStyle("-fx-opacity: 1.0;");
                    deleteButton.setVisible(false); // Initially hidden, shown on hover
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                }

                setGraphic(content);
            }
        }

        private void updateTimeLabel(Task task) {
            if (task.getEstimatedTime() > 0) {
                timeLabel.setText(String.format("%d/%dm", task.getTotalTimeSpent(), task.getEstimatedTime()));
                // Change color if over estimate
                if (task.isOverEstimate()) {
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #FF5722; -fx-font-weight: bold;");
                } else {
                    timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #4CAF50;");
                }
            } else if (task.getTotalTimeSpent() > 0) {
                timeLabel.setText(task.getTimeSpentFormatted());
                timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196F3;");
            } else {
                timeLabel.setText("");
            }
        }

        private void updateActiveIndicator(Task task) {
            if (task.isActive()) {
                activeIndicator.setFill(Color.web("#4CAF50"));
                activeIndicator.setVisible(true);

                // Add pulse animation for active task
                FadeTransition pulse = new FadeTransition(Duration.seconds(1), activeIndicator);
                pulse.setFromValue(0.3);
                pulse.setToValue(1.0);
                pulse.setCycleCount(Timeline.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();
            } else {
                activeIndicator.setFill(Color.TRANSPARENT);
                activeIndicator.setVisible(false);
            }
        }
    }
}