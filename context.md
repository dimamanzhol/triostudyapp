# Visual Study Dashboard & Timer - Project Context

## Project Overview

A comprehensive productivity application designed for students to manage their study sessions, track tasks, and visualize their academic progress. The application combines time management (Pomodoro technique), task organization, and data visualization to help students optimize their study habits.

## Core Objectives

- Provide an intuitive interface for managing study sessions
- Track productivity metrics and display meaningful statistics
- Offer a clean, modern UI with customizable themes
- Persist user data across application sessions
- Help students stay focused and organized

---

## Features & Functionality

### 1. Pomodoro Timer Module

**Description:** Interactive timer implementing the Pomodoro Technique for focused study sessions.

**Functions:**

- Start/pause/reset timer functionality
- Configurable work intervals (default: 25 minutes) and break intervals (5/15 minutes)
- Visual circular progress indicator with smooth animations
- Audio/visual notifications when sessions complete
- Automatic transition between work and break periods
- Session counter tracking consecutive pomodoros

**UI Components:**

- Circular progress ring (custom Canvas or ProgressIndicator)
- Digital time display
- Control buttons (Start, Pause, Reset, Settings)
- Session type indicator (Work/Short Break/Long Break)

---

### 2. Task Management System

**Description:** Comprehensive task list for tracking assignments, deadlines, and priorities.

**Functions:**

- Create, read, update, delete (CRUD) tasks
- Set task properties: title, description, due date, priority level
- Mark tasks as complete/incomplete
- Filter tasks by status (All/Active/Completed)
- Sort tasks by priority, due date, or creation date
- Color-coded priority levels (High/Medium/Low)
- Drag-and-drop reordering
- Search/filter functionality

**UI Components:**

- ListView or TableView for task display
- Custom cell rendering with checkboxes and labels
- Task creation dialog/form
- Priority indicator badges
- Due date visual warnings (overdue tasks highlighted)

---

### 3. Study Session Tracker

**Description:** Log and categorize completed study sessions for analytics.

**Functions:**

- Automatically log completed Pomodoro sessions
- Manual session entry with custom duration
- Categorize sessions by subject/topic
- Add notes/descriptions to sessions
- View session history
- Edit/delete past sessions
- Calculate total study time per day/week/month

**UI Components:**

- Session log table with columns: Date, Duration, Subject, Notes
- Quick-add session form
- Subject/category dropdown selector
- Date picker for historical entries

---

### 4. Statistics Dashboard

**Description:** Visual analytics showing study patterns and productivity trends.

**Functions:**

- Display total study time (today, this week, this month, all-time)
- Show study time distribution by subject (Pie Chart)
- Visualize daily study trends over time (Line Chart)
- Display task completion rates (Bar Chart)
- Show most productive hours/days
- Calculate average session length
- Track study streak (consecutive days)

**UI Components:**

- Multiple JavaFX Charts (PieChart, BarChart, LineChart, AreaChart)
- Summary cards with key metrics
- Date range selector for filtering data
- Export statistics as text report

---

### 5. Calendar View

**Description:** Monthly/weekly calendar displaying study sessions and task deadlines.

**Functions:**

- Display current month with day cells
- Show study sessions as colored blocks on dates
- Mark task deadlines with icons/indicators
- Click on date to view detailed session/task list
- Navigate between months (previous/next)
- Highlight current day
- Visual density indicator (more study = darker/highlighted cell)

**UI Components:**

- Custom GridPane-based calendar
- Day cell components with event indicators
- Month/year navigation controls
- Day detail popup/sidebar

---

### 6. Note-Taking System

**Description:** Simple rich text editor for quick notes and study materials.

**Functions:**

- Create, edit, save, delete notes
- Rich text formatting: bold, italic, underline, headers
- Bulleted and numbered lists
- HTML content rendering
- Link notes to subjects/tasks
- Search within notes
- Export notes as HTML

**UI Components:**

- WebView for rendering HTML content
- Toolbar with formatting buttons
- Notes list sidebar
- Text editing area

---

### 7. Settings & Customization

**Description:** User preferences and application configuration.

**Functions:**

- Adjust Pomodoro timer durations (work/break intervals)
- Enable/disable sound notifications
- Toggle dark/light theme
- Customize accent colors
- Set default study categories
- Configure dashboard layout
- Data backup/restore options

**UI Components:**

- Settings panel with sections
- Sliders for time adjustments
- Toggle switches
- Color pickers
- Theme preview

---

### 8. Data Persistence

**Description:** Save and load application data between sessions.

**Functions:**

- Serialize application state to JSON files
- Auto-save on data changes
- Load data on application startup
- Import/export data functionality
- Backup creation
- Data validation on load

**File Structure:**

```
data/
├── tasks.json          # All tasks
├── sessions.json       # Study session history
├── notes.json          # User notes
├── settings.json       # User preferences
└── backups/           # Automatic backups
```

---

## Technical Stack

### Core Technologies

- **Java 11+** - Programming language
- **JavaFX 17+** - UI framework
- **FXML** - UI layout definition (optional, can use pure Java)
- **CSS3** - Styling and theming

### JavaFX Components Used

- **Application & Stage** - Main application window
- **Scene & Scene Builder** - UI composition
- **Layout Managers:**

  - BorderPane (main layout)
  - VBox/HBox (vertical/horizontal layouts)
  - GridPane (calendar, forms)
  - StackPane (overlays, layers)
  - AnchorPane (flexible positioning)

- **Controls:**

  - Button, Label, TextField, TextArea
  - ListView, TableView
  - ComboBox, CheckBox, RadioButton
  - DatePicker, Spinner
  - ProgressIndicator, ProgressBar
  - MenuBar, MenuItem
  - TabPane, SplitPane

- **Charts:**

  - PieChart (subject distribution)
  - BarChart (task completion, weekly hours)
  - LineChart (study trends over time)
  - AreaChart (cumulative study time)

- **Advanced Components:**
  - WebView (rich text notes)
  - Canvas (custom timer visualization)
  - Timeline & Animation (smooth transitions)

### Built-in Java Libraries

- **java.io / java.nio** - File operations
- **java.time** - Date and time handling
- **java.util** - Collections, data structures
- **org.json** or manual JSON parsing - Data serialization
- **java.util.prefs.Preferences** - User preferences storage (alternative to JSON)

### Styling

- **CSS** - Custom styling for all components
- **Theme files:**
  - `light-theme.css`
  - `dark-theme.css`
- **Custom fonts** (optional, embedded in resources)

---

## Project Structure

```
StudyDashboard/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── app/
│   │   │   │   └── Main.java
│   │   │   ├── controllers/
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── TimerController.java
│   │   │   │   ├── TaskController.java
│   │   │   │   ├── StatsController.java
│   │   │   │   ├── CalendarController.java
│   │   │   │   └── NotesController.java
│   │   │   ├── models/
│   │   │   │   ├── Task.java
│   │   │   │   ├── StudySession.java
│   │   │   │   ├── Note.java
│   │   │   │   └── Settings.java
│   │   │   ├── services/
│   │   │   │   ├── DataManager.java
│   │   │   │   ├── TimerService.java
│   │   │   │   └── StatisticsService.java
│   │   │   └── utils/
│   │   │       ├── JsonHandler.java
│   │   │       ├── ThemeManager.java
│   │   │       └── NotificationManager.java
│   │   └── resources/
│   │       ├── fxml/
│   │       │   ├── dashboard.fxml
│   │       │   ├── timer.fxml
│   │       │   ├── tasks.fxml
│   │       │   └── stats.fxml
│   │       ├── css/
│   │       │   ├── light-theme.css
│   │       │   └── dark-theme.css
│   │       ├── images/
│   │       │   └── icons/
│   │       └── sounds/
│   │           ├── session-complete.mp3
│   │           └── break-start.mp3
└── data/
    └── (generated at runtime)
```

---

## Development Timeline (3 Days)

### Day 1: Foundation & Timer

- Set up project structure
- Implement Pomodoro timer with UI
- Create task management system
- Basic CSS styling
- Data models

### Day 2: Data & Visualization

- Study session tracker
- Statistics dashboard with charts
- Calendar view
- Data persistence (JSON)
- Integration between modules

### Day 3: Polish & Advanced Features

- Note-taking system with WebView
- Settings panel
- Theme switching (dark/light mode)
- Animations and transitions
- Bug fixes and refinement
- Export/import functionality

---

## Key Design Patterns

### MVC (Model-View-Controller)

- **Models:** Data classes (Task, StudySession, Note)
- **Views:** FXML layouts or JavaFX UI code
- **Controllers:** Handle user interactions and business logic

### Observer Pattern

- Timer updates trigger UI refreshes
- Data changes notify multiple views
- Statistics automatically update when sessions are logged

### Singleton Pattern

- DataManager (central data access point)
- ThemeManager (global theme state)
- Settings (user preferences)

---

## Technical Considerations

### Performance

- Efficient chart rendering with data aggregation
- Lazy loading for large session histories
- Debounced auto-save to prevent excessive file writes

### User Experience

- Smooth animations (FadeTransition, ScaleTransition)
- Keyboard shortcuts for common actions
- Responsive layout for different window sizes
- Clear visual feedback for all actions

### Data Integrity

- Input validation for all forms
- Graceful error handling for file I/O
- Automatic backups before overwriting data
- Default values for corrupted settings

---

## Potential Extensions (Post-Deadline)

- Cloud sync (Google Drive API)
- Study goals and achievements system
- Collaboration features (shared tasks)
- Mobile companion app
- AI-powered study recommendations
- Integration with calendar services (Google Calendar)
- Spaced repetition flashcard system
- Focus mode (block distracting apps)

---

## Success Criteria

✅ **Functionality:** All core features working without crashes  
✅ **UI/UX:** Intuitive, visually appealing interface  
✅ **Code Quality:** Clean, well-organized, documented code  
✅ **Persistence:** Data saved and loaded correctly  
✅ **Performance:** Smooth animations, responsive interactions  
✅ **Creativity:** Unique features that stand out

---

## Resources & References

### JavaFX Documentation

- [Official JavaFX Docs](https://openjfx.io/javadoc/17/)
- [JavaFX CSS Reference Guide](https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/doc-files/cssref.html)

### Design Inspiration

- Material Design principles
- Modern productivity apps (Notion, Todoist, Forest)
- Minimalist UI patterns

### Learning Resources

- JavaFX Charts tutorial
- WebView HTML integration examples
- JSON parsing in Java
- CSS animations and transitions

---

**Good luck with your project! This should showcase your JavaFX skills while creating something genuinely useful. 🚀**
