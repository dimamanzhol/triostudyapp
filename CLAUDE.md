# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TrioStudyApp is a JavaFX-based productivity application for students using the Pomodoro technique with task management, study session tracking, and statistics visualization. The app supports different session types (Work and Study) with configurable timer durations, task time tracking, and dark/light theme switching.

## Build and Run Commands

```bash
# Compile the project
mvn compile

# Run the application (recommended)
mvn javafx:run

# Clean and rebuild
mvn clean compile
```

**Alternative run method (if Maven has issues):**
```bash
# Set JavaFX path (macOS example after: brew install openjfx)
export PATH_TO_FX=/path/to/javafx/sdk/lib

# Run directly
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -cp target/classes app.Main
```

## System Requirements

- **Java**: 21 (configured in pom.xml)
- **JavaFX**: 21.0.1
- **Maven**: 3.6+
- **org.json library**: 20231013 (for JSON data persistence)

## Architecture

### MVC Pattern

The application follows a Model-View-Controller architecture:

- **Models** (`models/`): Data entities with JavaFX properties for binding
  - `Task`: Task with title, description, completion status, and time tracking (estimated time, time spent)
  - `StudySession`: Logged session with duration, subject, session type (WORK/STUDY), and project name
  - `SessionType`: Enum defining session types with default timer configurations
  - `SessionConfiguration`: Configurable work/break durations for each session type
  - `TaskType`: Enum for task categorization

- **Views/Controllers** (`controllers/`): JavaFX UI components created programmatically (no FXML)
  - `MainController`: Primary controller managing the timer UI, task list, and theme switching
  - `CalloutSelectionController`: Session type selection dialog

- **Services** (`services/`): Business logic and data persistence
  - `TimerService`: Pomodoro timer with pause/reset, session type switching, and callback support
  - `DataManager`: JSON-based persistence for tasks and sessions

### Key Design Patterns

- **Observer Pattern**: JavaFX properties (`BooleanProperty`, `StringProperty`, `IntegerProperty`) enable reactive UI updates
- **Singleton-style Services**: Single `TimerService` and `DataManager` instances injected into controllers
- **Callback Pattern**: `TimerService.setOnSessionComplete(Runnable)` for handling timer completion events

### Data Flow

1. User actions trigger UI event handlers in `MainController`
2. Controller updates models via `DataManager` or controls `TimerService`
3. Model changes (through JavaFX properties) automatically update UI bindings
4. `DataManager` persists changes to JSON files in `data/` directory

## Important Implementation Details

### Timer Service Behavior

- `TimerService` runs on a background daemon thread with 1-second intervals
- UI updates must use `Platform.runLater()` for thread safety
- Timer completion callback (`onSessionComplete`) is invoked on the JavaFX application thread
- Session type (WORK/STUDY) determines default durations:
  - WORK: 25 min work, 5 min break
  - STUDY: 45 min work, 10 min break

### Task Time Tracking

- Tasks support time estimation and tracking actual time spent
- Active task selection allows timer completion to automatically add time to the focused task
- When `totalTimeSpent >= estimatedTime`, a confirmation dialog asks to mark the task complete
- Task cells display time progress: `X/Ym` (spent/estimated) or just `Xm` if no estimate

### Theme Management

- Theme state stored in `data/theme.json`
- Dark mode implemented by adding `"dark"` CSS class to components
- Theme toggle button (🌙/☀️) in top-right of timer section
- Custom cell renderers must check `isDarkMode` flag to apply appropriate styles

### Data Persistence

JSON files in `data/` directory:
- `tasks.json`: Task list with time tracking properties
- `sessions.json`: Study session history with session type and project name
- `theme.json`: Dark mode preference

**Important**: Always call `dataManager.saveData()` before application exit (handled in `Main.setOnCloseRequest`)

### Custom ListView Cells

`MainController.TaskListCell` is a custom ListCell implementation featuring:
- Checkbox for completion toggle with animation
- Click-to-focus task selection
- Time tracking display with color coding (green=on track, orange=over estimate)
- Active indicator circle with pulse animation for focused task
- Delete button with fade-out animation
- Hover effects and dynamic styling based on task state

## File Structure

```
src/main/java/
├── app/
│   └── Main.java                    # Application entry point, sets up scene
├── controllers/
│   ├── MainController.java          # Main UI controller (600+ lines)
│   └── CalloutSelectionController.java
├── models/
│   ├── Task.java                    # Task with time tracking properties
│   ├── StudySession.java            # Session with type and project
│   ├── SessionType.java             # Enum: WORK, STUDY
│   ├── SessionConfiguration.java    # Timer settings per session type
│   └── TaskType.java                # Enum for task categories
└── services/
    ├── DataManager.java             # JSON persistence
    └── TimerService.java            # Timer logic with background thread

src/main/resources/
└── css/
    └── styles.css                   # UI styling with dark theme support

data/                                 # Generated at runtime
├── tasks.json
├── sessions.json
└── theme.json
```

## Common Development Tasks

### Adding a new UI component
1. Create component in `MainController`'s section creation methods
2. Apply CSS classes for styling
3. Add dark theme support by checking `isDarkMode` in `applyTheme()`

### Modifying timer behavior
1. Update `TimerService` for logic changes
2. Adjust `SessionType.getDefaultWorkDuration()` or `getDefaultBreakDuration()` for new defaults
3. Use `Platform.runLater()` for any UI updates from the timer thread

### Adding new task properties
1. Add fields to `Task.java` with JavaFX properties
2. Update `DataManager.loadTasks()` and `saveTasks()` for persistence
3. Modify `TaskListCell.updateItem()` to display new properties

### Session type customization
1. Add new value to `SessionType` enum with icon and default durations
2. Update `CalloutSelectionController` to include new option
3. Configure default work/break durations in enum methods

## CSS Styling Conventions

- Component classes: `.main-container`, `.timer-section`, `.tasks-section`, `.task-cell`
- State classes: `.dark` (for dark mode), `.completed` (for completed tasks)
- Button classes: `.timer-button`, `.start-button`, `.pause-button`, `.reset-button`, `.add-button`, `.delete-button`, `.theme-toggle`
- Dark mode is applied by adding `"dark"` class, not by replacing stylesheets

## Platform-Specific Notes

- **macOS**: JavaFX may show native access warnings - these can be safely ignored
- Maven plugin configured with `--enable-native-access=javafx.graphics` VM argument
- Application automatically saves data on window close
