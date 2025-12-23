package services;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * LocalStorage service for managing application data persistence.
 * Handles file I/O operations, backup creation, and data export/import.
 */
public class LocalStorage {
    private static final String DATA_DIR = "data";
    private static final String BACKUP_DIR = "data/backups";
    private static final String EXPORT_DIR = "data/exports";

    private static final int MAX_BACKUPS = 10;

    /**
     * Ensures all required directories exist
     */
    public void ensureDirectoriesExist() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
            Files.createDirectories(Paths.get(EXPORT_DIR));
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
        }
    }

    /**
     * Read JSON content from a file
     */
    public JSONObject readJsonFile(String filename) {
        Path filePath = Paths.get(DATA_DIR, filename);
        File file = filePath.toFile();

        if (!file.exists()) {
            return new JSONObject();
        }

        try {
            String content = new String(Files.readAllBytes(filePath));
            if (content.trim().isEmpty()) {
                return new JSONObject();
            }
            return new JSONObject(content);
        } catch (Exception e) {
            System.err.println("Error reading " + filename + ": " + e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * Write JSON content to a file
     */
    public void writeJsonFile(String filename, JSONObject json) {
        try {
            ensureDirectoriesExist();
            Path filePath = Paths.get(DATA_DIR, filename);

            // Create backup before overwriting
            if (Files.exists(filePath)) {
                createBackup(filename);
            }

            Files.write(filePath, json.toString(2).getBytes());
        } catch (IOException e) {
            System.err.println("Error writing " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Write string content to a file
     */
    public void writeStringToFile(String filename, String content) {
        try {
            ensureDirectoriesExist();
            Path filePath = Paths.get(DATA_DIR, filename);
            Files.write(filePath, content.getBytes());
        } catch (IOException e) {
            System.err.println("Error writing " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Create a backup of a file with timestamp
     */
    private void createBackup(String filename) {
        try {
            Path originalPath = Paths.get(DATA_DIR, filename);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFilename = filename.replace(".json", "_" + timestamp + ".json");
            Path backupPath = Paths.get(BACKUP_DIR, backupFilename);

            Files.copy(originalPath, backupPath);

            // Clean old backups
            cleanOldBackups(filename);
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
        }
    }

    /**
     * Remove old backups, keeping only the most recent MAX_BACKUPS
     */
    private void cleanOldBackups(String filename) {
        try {
            File backupDir = new File(BACKUP_DIR);
            File[] backupFiles = backupDir.listFiles((dir, name) ->
                name.startsWith(filename.replace(".json", "")) && name.endsWith(".json")
            );

            if (backupFiles != null && backupFiles.length > MAX_BACKUPS) {
                // Sort by last modified (oldest first)
                java.util.Arrays.sort(backupFiles, (a, b) ->
                    Long.compare(a.lastModified(), b.lastModified())
                );

                // Delete oldest backups
                for (int i = 0; i < backupFiles.length - MAX_BACKUPS; i++) {
                    backupFiles[i].delete();
                }
            }
        } catch (Exception e) {
            System.err.println("Error cleaning backups: " + e.getMessage());
        }
    }

    /**
     * Export data to a timestamped JSON file in the exports directory
     */
    public String exportData(Map<String, JSONObject> dataMap) {
        try {
            ensureDirectoriesExist();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String exportFilename = "export_" + timestamp + ".json";
            Path exportPath = Paths.get(EXPORT_DIR, exportFilename);

            JSONObject exportData = new JSONObject();
            for (Map.Entry<String, JSONObject> entry : dataMap.entrySet()) {
                exportData.put(entry.getKey(), entry.getValue());
            }

            // Add metadata
            JSONObject metadata = new JSONObject();
            metadata.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            metadata.put("version", "1.0");
            exportData.put("_metadata", metadata);

            Files.write(exportPath, exportData.toString(2).getBytes());
            return exportPath.toAbsolutePath().toString();
        } catch (IOException e) {
            System.err.println("Error exporting data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Import data from an export file
     */
    public Map<String, JSONObject> importData(String exportFilePath) {
        Map<String, JSONObject> result = new HashMap<>();
        try {
            Path importPath = Paths.get(exportFilePath);
            String content = new String(Files.readAllBytes(importPath));
            JSONObject importData = new JSONObject(content);

            // Remove metadata and process data
            importData.remove("_metadata");

            for (String key : importData.keySet()) {
                result.put(key, importData.getJSONObject(key));
            }

            return result;
        } catch (Exception e) {
            System.err.println("Error importing data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all export files
     */
    public File[] getExportFiles() {
        File exportDir = new File(EXPORT_DIR);
        if (!exportDir.exists()) {
            return new File[0];
        }
        File[] files = exportDir.listFiles((dir, name) -> name.endsWith(".json"));
        return files != null ? files : new File[0];
    }

    /**
     * Get all backup files for a specific data file
     */
    public File[] getBackupFiles(String filename) {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            return new File[0];
        }
        String prefix = filename.replace(".json", "");
        File[] files = backupDir.listFiles((dir, name) ->
            name.startsWith(prefix) && name.endsWith(".json")
        );
        return files != null ? files : new File[0];
    }

    /**
     * Restore from a backup file
     */
    public boolean restoreFromBackup(String backupFilename, String targetFilename) {
        try {
            Path backupPath = Paths.get(BACKUP_DIR, backupFilename);
            Path targetPath = Paths.get(DATA_DIR, targetFilename);

            if (!Files.exists(backupPath)) {
                return false;
            }

            // Create backup of current file before restoring
            if (Files.exists(targetPath)) {
                createBackup(targetFilename);
            }

            Files.copy(backupPath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error restoring from backup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clear all data (use with caution)
     */
    public void clearAllData() {
        try {
            File dataDir = new File(DATA_DIR);
            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".json"));

            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            System.err.println("Error clearing data: " + e.getMessage());
        }
    }

    /**
     * Get storage statistics
     */
    public JSONObject getStorageStats() {
        JSONObject stats = new JSONObject();

        try {
            File dataDir = new File(DATA_DIR);
            File[] dataFiles = dataDir.listFiles((dir, name) -> name.endsWith(".json") && !new File(BACKUP_DIR).equals(dir));
            long totalSize = 0;

            if (dataFiles != null) {
                for (File file : dataFiles) {
                    totalSize += file.length();
                }
                stats.put("dataFiles", dataFiles.length);
            } else {
                stats.put("dataFiles", 0);
            }

            stats.put("totalSizeBytes", totalSize);
            stats.put("totalSizeKB", String.format("%.2f", totalSize / 1024.0));

            File backupDir = new File(BACKUP_DIR);
            File[] backupFiles = backupDir.listFiles();
            stats.put("backupFiles", backupFiles != null ? backupFiles.length : 0);

            File exportDir = new File(EXPORT_DIR);
            File[] exportFiles = exportDir.listFiles();
            stats.put("exportFiles", exportFiles != null ? exportFiles.length : 0);

        } catch (Exception e) {
            System.err.println("Error getting storage stats: " + e.getMessage());
        }

        return stats;
    }
}
