# Visual Study Dashboard & Timer - MVP

A simple JavaFX productivity application for students using the Pomodoro technique with task management.

## Features

- **Pomodoro Timer**: 25-minute work sessions with 5-minute breaks
- **Task Management**: Add, delete, and complete tasks
- **Study Session Tracking**: Automatic logging of completed sessions
- **Daily Statistics**: View today's study time and completed tasks
- **Data Persistence**: Tasks and sessions saved to JSON files

## Technology Stack

- Java 11+
- JavaFX 17+
- Maven
- JSON for data storage

## Project Structure

```
src/main/java/
├── app/
│   └── Main.java                    # Application entry point
├── controllers/
│   └── MainController.java          # Main UI controller
├── models/
│   ├── Task.java                    # Task data model
│   └── StudySession.java            # Study session model
└── services/
    ├── DataManager.java             # JSON data persistence
    └── TimerService.java            # Pomodoro timer logic

src/main/resources/css/
└── styles.css                       # UI styling
```

## Running the Application

### Prerequisites
- Java 11 or higher
- JavaFX 17 or higher
- Maven 3.6+

### Compile and Run
```bash
# Compile the project
mvn compile

# Run the application
mvn javafx:run
```

## Usage

1. **Timer Controls**:
   - Start: Begin a work session
   - Pause: Pause the current session
   - Reset: Reset timer to initial state

2. **Task Management**:
   - Type task name in the text field
   - Click "Add Task" to create a new task
   - Check the checkbox to mark task as complete
   - Click "Delete" to remove a task

3. **Statistics**:
   - View total study time for today
   - Track number of completed tasks

## Data Storage

The application stores data in JSON files:
- `data/tasks.json` - Task list
- `data/sessions.json` - Study session history

## Troubleshooting

### Common Issues

1. **Java Version Mismatch**: Ensure you have Java 17+ installed
2. **JavaFX Not Found**: Make sure JavaFX dependencies are properly configured
3. **Module System Errors**: Try using the simple run script instead of Maven

### Running Without Maven

If Maven causes issues, try this alternative approach:

```bash
# 1. Install JavaFX SDK (macOS example)
brew install openjfx

# 2. Set environment variables
export PATH_TO_FX=/path/to/javafx/sdk/lib

# 3. Run directly
java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.fxml -cp target/classes app.Main
```

### IDE Configuration

For IntelliJ IDEA or Eclipse:
1. Add JavaFX as a library/module dependency
2. Set VM options: `--add-modules javafx.controls,javafx.fxml`
3. Ensure JDK 17+ is selected

### macOS Specific Issues

If you encounter native access warnings or crashes on macOS:
- Try running with the provided script: `./run.sh`
- Or use Maven: `mvn javafx:run`
- These warnings can usually be ignored as they don't affect functionality

## MVP Notes

This is a minimal viable product (MVP) focused on core functionality. Future enhancements may include:
- Charts and visualization
- Calendar view
- Note-taking system
- Advanced settings
- Data export/import

## Troubleshooting

### macOS Native Access Warning
If you see warnings about native access, this is normal for JavaFX applications and can be safely ignored.

### Compilation Errors
Ensure you have Java 11+ and JavaFX 17+ properly installed and configured in your IDE or environment.

## License

This project is created for educational purposes.