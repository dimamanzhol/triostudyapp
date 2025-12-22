package controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import models.SessionType;
import models.StudySession;
import services.DataManager;

import java.time.LocalDate;
import java.util.List;

public class CalloutSelectionController {
    private Dialog<SessionType> dialog;
    private SessionType selectedSessionType;

    public CalloutSelectionController(DataManager dataManager) {
        createDialog(dataManager);
    }

    private void createDialog(DataManager dataManager) {
        dialog = new Dialog<>();
        dialog.setTitle("Choose Session Type");
        dialog.setHeaderText("What type of session would you like to start?");
        dialog.setResizable(false);

        // Set dialog size
        dialog.getDialogPane().setPrefSize(500, 400);

        // Create custom content
        VBox content = createContent(dataManager);

        // Add content to dialog
        dialog.getDialogPane().setContent(content);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().setAll(
            new ButtonType("Start Session", ButtonBar.ButtonData.OK_DONE),
            ButtonType.CANCEL
        );

        // Handle result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return selectedSessionType;
            }
            return null;
        });
    }

    private VBox createContent(DataManager dataManager) {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.CENTER);

        // Add description
        Label description = new Label("Select a session type to begin tracking your productivity:");
        description.setWrapText(true);
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        // Create session type cards
        HBox cardsContainer = new HBox(20);
        cardsContainer.setAlignment(Pos.CENTER);

        VBox workCard = createSessionCard(SessionType.WORK, dataManager);
        VBox studyCard = createSessionCard(SessionType.STUDY, dataManager);

        cardsContainer.getChildren().addAll(workCard, studyCard);

        // Add recent sessions info
        VBox recentSessions = createRecentSessionsSection(dataManager);

        mainContainer.getChildren().addAll(description, cardsContainer, recentSessions);

        return mainContainer;
    }

    private VBox createSessionCard(SessionType sessionType, DataManager dataManager) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefSize(200, 150);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " +
                     "-fx-border-color: #e0e0e0; -fx-border-width: 2px; -fx-border-radius: 10px; " +
                     "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Icon
        Label iconLabel = new Label(sessionType.getIcon());
        iconLabel.setFont(new Font(48));
        iconLabel.setStyle("-fx-text-fill: " + getSessionTypeColor(sessionType) + ";");

        // Title
        Label titleLabel = new Label(sessionType.getDisplayName());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        // Description
        Label descLabel = new Label(sessionType.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-alignment: center;");
        descLabel.setMaxWidth(160);

        // Stats
        int todayMinutes = getTodayMinutesForSessionType(sessionType, dataManager);
        Label statsLabel = new Label(String.format("Today: %d min", todayMinutes));
        statsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        statsLabel.setStyle("-fx-text-fill: " + getSessionTypeColor(sessionType) + ";");

        card.getChildren().addAll(iconLabel, titleLabel, descLabel, statsLabel);

        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + getSessionTypeLightColor(sessionType) + "; " +
                         "-fx-background-radius: 10px; -fx-border-color: " + getSessionTypeColor(sessionType) + "; " +
                         "-fx-border-width: 2px; -fx-border-radius: 10px; -fx-cursor: hand; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 0);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " +
                         "-fx-border-color: #e0e0e0; -fx-border-width: 2px; -fx-border-radius: 10px; " +
                         "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        });

        // Add click handler
        card.setOnMouseClicked(e -> {
            selectedSessionType = sessionType;
            // Update visual selection
            updateCardSelection(card, workCard, studyCard, sessionType);
        });

        return card;
    }

    private void updateCardSelection(VBox selectedCard, VBox workCard, VBox studyCard, SessionType sessionType) {
        // Reset all cards
        resetCardStyle(workCard);
        resetCardStyle(studyCard);

        // Highlight selected card
        selectedCard.setStyle("-fx-background-color: " + getSessionTypeLightColor(sessionType) + "; " +
                           "-fx-background-radius: 10px; -fx-border-color: " + getSessionTypeColor(sessionType) + "; " +
                           "-fx-border-width: 3px; -fx-border-radius: 10px; -fx-cursor: hand; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 0);");
    }

    private void resetCardStyle(VBox card) {
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; " +
                    "-fx-border-color: #e0e0e0; -fx-border-width: 2px; -fx-border-radius: 10px; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
    }

    private VBox createRecentSessionsSection(DataManager dataManager) {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Recent Sessions Today");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #666666;");

        HBox recentStats = new HBox(30);
        recentStats.setAlignment(Pos.CENTER);

        int workMinutes = getTodayMinutesForSessionType(SessionType.WORK, dataManager);
        int studyMinutes = getTodayMinutesForSessionType(SessionType.STUDY, dataManager);

        Label workStats = new Label(String.format("%s %d min", SessionType.WORK.getIcon(), workMinutes));
        workStats.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getSessionTypeColor(SessionType.WORK) + ";");

        Label studyStats = new Label(String.format("%s %d min", SessionType.STUDY.getIcon(), studyMinutes));
        studyStats.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getSessionTypeColor(SessionType.STUDY) + ";");

        recentStats.getChildren().addAll(workStats, studyStats);
        section.getChildren().addAll(titleLabel, recentStats);

        return section;
    }

    private int getTodayMinutesForSessionType(SessionType sessionType, DataManager dataManager) {
        LocalDate today = LocalDate.now();
        List<StudySession> todaySessions = dataManager.getStudySessionsForDate(today);

        return todaySessions.stream()
                .filter(session -> session.getSessionType() == sessionType)
                .mapToInt(StudySession::getDurationMinutes)
                .sum();
    }

    private String getSessionTypeColor(SessionType sessionType) {
        switch (sessionType) {
            case WORK:
                return "#2196F3"; // Blue
            case STUDY:
                return "#4CAF50"; // Green
            default:
                return "#666666";
        }
    }

    private String getSessionTypeLightColor(SessionType sessionType) {
        switch (sessionType) {
            case WORK:
                return "#E3F2FD"; // Light blue
            case STUDY:
                return "#E8F5E8"; // Light green
            default:
                return "#f5f5f5";
        }
    }

    public SessionType showAndWait() {
        // Default selection
        selectedSessionType = SessionType.WORK;

        return dialog.showAndWait().orElse(null);
    }
}