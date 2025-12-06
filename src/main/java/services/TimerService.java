package services;

import javafx.application.Platform;
import javafx.beans.property.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimerService {
    private static final int WORK_DURATION_MINUTES = 25;
    private static final int BREAK_DURATION_MINUTES = 5;

    private final IntegerProperty remainingSeconds = new SimpleIntegerProperty(WORK_DURATION_MINUTES * 60);
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty isBreak = new SimpleBooleanProperty(false);
    private final StringProperty timeDisplay = new SimpleStringProperty(formatTime(WORK_DURATION_MINUTES * 60));

    private Thread timerThread;
    private LocalDateTime startTime;
    private Runnable onSessionComplete;

    public TimerService() {
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
        int duration = isBreak.get() ? BREAK_DURATION_MINUTES : WORK_DURATION_MINUTES;
        remainingSeconds.set(duration * 60);
    }

    public void switchToBreak() {
        pause();
        isBreak.set(true);
        remainingSeconds.set(BREAK_DURATION_MINUTES * 60);
    }

    public void switchToWork() {
        pause();
        isBreak.set(false);
        remainingSeconds.set(WORK_DURATION_MINUTES * 60);
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
        return WORK_DURATION_MINUTES;
    }

    public int getBreakDurationMinutes() {
        return BREAK_DURATION_MINUTES;
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