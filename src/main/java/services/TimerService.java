package services;

import javafx.application.Platform;
import javafx.beans.property.*;
import models.SessionType;
import models.SessionConfiguration;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimerService {
    private SessionConfiguration sessionConfiguration;
    private SessionType currentSessionType;

    private final IntegerProperty remainingSeconds = new SimpleIntegerProperty(25 * 60);
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty isBreak = new SimpleBooleanProperty(false);
    private final StringProperty timeDisplay = new SimpleStringProperty(formatTime(25 * 60));

    private Thread timerThread;
    private LocalDateTime startTime;
    private Runnable onSessionComplete;

    public TimerService() {
        this(SessionType.WORK);
    }

    public TimerService(SessionType sessionType) {
        this.currentSessionType = sessionType;
        this.sessionConfiguration = new SessionConfiguration(sessionType);
        remainingSeconds.set(sessionConfiguration.getWorkDurationMinutes() * 60);
        timeDisplay.set(formatTime(sessionConfiguration.getWorkDurationMinutes() * 60));

        remainingSeconds.addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> timeDisplay.set(formatTime(newVal.intValue())));
        });
    }

    public void start() {
        if (!isRunning.get()) {
            isRunning.set(true);
            startTime = LocalDateTime.now();
            timerThread = new Thread(this::runTimer);
            timerThread.setDaemon(true);
            timerThread.start();
        }
    }

    public void pause() {
        isRunning.set(false);
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }

    public void reset() {
        pause();
        int duration = isBreak.get() ? sessionConfiguration.getBreakDurationMinutes() : sessionConfiguration.getWorkDurationMinutes();
        remainingSeconds.set(duration * 60);
    }

    public void switchToBreak() {
        pause();
        isBreak.set(true);
        remainingSeconds.set(sessionConfiguration.getBreakDurationMinutes() * 60);
    }

    public void switchToWork() {
        pause();
        isBreak.set(false);
        remainingSeconds.set(sessionConfiguration.getWorkDurationMinutes() * 60);
    }

    private void runTimer() {
        while (isRunning.get() && remainingSeconds.get() > 0) {
            try {
                Thread.sleep(1000);
                if (isRunning.get()) {
                    Platform.runLater(() -> remainingSeconds.set(remainingSeconds.get() - 1));
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        if (remainingSeconds.get() <= 0) {
            Platform.runLater(() -> {
                isRunning.set(false);
                if (onSessionComplete != null) {
                    onSessionComplete.run();
                }
            });
        }
    }

    private static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getWorkDurationMinutes() {
        return sessionConfiguration.getWorkDurationMinutes();
    }

    public int getBreakDurationMinutes() {
        return sessionConfiguration.getBreakDurationMinutes();
    }

    // NEW: Session type management
    public SessionType getCurrentSessionType() {
        return currentSessionType;
    }

    public void setSessionType(SessionType sessionType) {
        pause();
        this.currentSessionType = sessionType;
        this.sessionConfiguration = new SessionConfiguration(sessionType);
        reset();
    }

    public SessionConfiguration getSessionConfiguration() {
        return sessionConfiguration;
    }

    public void setSessionConfiguration(SessionConfiguration sessionConfiguration) {
        pause();
        this.sessionConfiguration = sessionConfiguration;
        this.currentSessionType = sessionConfiguration.getSessionType();
        reset();
    }

    public int getElapsedMinutes() {
        if (startTime == null) return 0;
        return (int) Duration.between(startTime, LocalDateTime.now()).toMinutes();
    }

    public void setOnSessionComplete(Runnable callback) {
        this.onSessionComplete = callback;
    }

    public IntegerProperty remainingSecondsProperty() {
        return remainingSeconds;
    }

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }

    public BooleanProperty isBreakProperty() {
        return isBreak;
    }

    public StringProperty timeDisplayProperty() {
        return timeDisplay;
    }
}