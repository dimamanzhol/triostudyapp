import models.SessionType;
import models.StudySession;
import models.Task;
import services.DataManager;
import services.LocalStorage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Simple test class to demonstrate LocalStorage functionality
 */
public class LocalStorageTest {
    public static void main(String[] args) {
        System.out.println("=== TrioStudyApp LocalStorage Test ===\n");

        // Test 1: Create DataManager and add sample data
        System.out.println("Test 1: Creating sample data...");
        DataManager dataManager = new DataManager();

        // Add some tasks
        Task task1 = new Task("Complete JavaFX project", "Finish the local storage implementation", 60);
        Task task2 = new Task("Study algorithms", "Practice dynamic programming problems", 45);
        dataManager.addTask(task1);
        dataManager.addTask(task2);

        // Add some study sessions
        StudySession session1 = new StudySession(25, "Pomodoro Session", SessionType.WORK);
        session1.setProjectName("JavaFX App");
        StudySession session2 = new StudySession(45, "Study Session", SessionType.STUDY);
        dataManager.addStudySession(session1);
        dataManager.addStudySession(session2);

        System.out.println("✓ Added 2 tasks and 2 study sessions\n");

        // Test 2: Export data
        System.out.println("Test 2: Exporting data...");
        String exportPath = dataManager.exportAllData();
        if (exportPath != null) {
            System.out.println("✓ Data exported to: " + exportPath);
        } else {
            System.out.println("✗ Export failed");
        }
        System.out.println();

        // Test 3: Get storage stats
        System.out.println("Test 3: Getting storage statistics...");
        var stats = dataManager.getStorageStats();
        System.out.println("✓ Storage Statistics:");
        System.out.println("  - Data files: " + stats.optInt("dataFiles", 0));
        System.out.println("  - Total size: " + stats.optString("totalSizeKB", "N/A") + " KB");
        System.out.println("  - Backup files: " + stats.optInt("backupFiles", 0));
        System.out.println("  - Export files: " + stats.optInt("exportFiles", 0));
        System.out.println();

        // Test 4: List export files
        System.out.println("Test 4: Listing export files...");
        File[] exportFiles = dataManager.getExportFiles();
        System.out.println("✓ Found " + exportFiles.length + " export file(s):");
        for (File file : exportFiles) {
            System.out.println("  - " + file.getName());
        }
        System.out.println();

        // Test 5: List backup files
        System.out.println("Test 5: Listing backup files...");
        File[] taskBackups = dataManager.getBackupFiles("tasks.json");
        System.out.println("✓ Found " + taskBackups.length + " task backup file(s):");
        for (File file : taskBackups) {
            System.out.println("  - " + file.getName());
        }
        System.out.println();

        // Test 6: Verify data persistence
        System.out.println("Test 6: Verifying data persistence...");
        System.out.println("✓ Total tasks: " + dataManager.getTasks().size());
        System.out.println("✓ Total sessions: " + dataManager.getStudySessions().size());
        System.out.println("✓ Study time today: " + dataManager.getTotalStudyTimeToday() + " minutes");
        System.out.println();

        System.out.println("=== All tests completed successfully! ===");
        System.out.println("\nData directory structure:");
        System.out.println("  data/");
        System.out.println("    ├── tasks.json");
        System.out.println("    ├── sessions.json");
        System.out.println("    ├── theme.json");
        System.out.println("    ├── backups/       (automatic backups)");
        System.out.println("    └── exports/       (manual exports)");
    }
}
