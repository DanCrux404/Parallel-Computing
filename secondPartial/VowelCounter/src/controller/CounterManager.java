package controller;

import Util.TimeHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import model.AppStatus;
import model.VowelResult;
import processing.ConcurrentCounter;
import processing.SequentialCounter;
import vowelcounter.windows.MainWindow;
import vowelcounter.windows.StatsWindow;

/**
 *
 * @author dante
 */
// Brain of the application — intermediary between processing and UI
// Same pattern as SimulationManager in previous projects
// Neither counters touch Swing, nor UI knows about processing
public class CounterManager {

    private final MainWindow mainWindow;
    private final StatsWindow statsWindow;

    // Files loaded by user — same for both sequential and concurrent
    private List<File> loadedFiles = new ArrayList<>();

    // Results from last run — sequential or concurrent
    private List<VowelResult> lastResults = new ArrayList<>();

    // Saved times for comparison
    private long sequentialTime = 0;
    private long concurrentTime = 0;

    // Counters — reused across runs
    private final SequentialCounter sequentialCounter = new SequentialCounter();
    private final ConcurrentCounter concurrentCounter = new ConcurrentCounter();

    public CounterManager(MainWindow mainWindow, StatsWindow statsWindow) {
        this.mainWindow = mainWindow;
        this.statsWindow = statsWindow;

        setupListeners();
    }
    
// == Setup listeners for both counters ============================
    // Same callback pattern as Buffer/Database in previous projects

    private void setupListeners() {

        // Sequential listener
        sequentialCounter.setListener(new SequentialCounter.SequentialListener() {
            @Override
            public void onFileStarted(int fileIndex) {
                // Update UI — file is now processing
                SwingUtilities.invokeLater(()
                        -> mainWindow.updateFileStatus(fileIndex, VowelResult.Status.PROCESSING)
                );
            }

            @Override
            public void onFileFinished(int fileIndex, VowelResult result) {
                // Update UI — file done, show results
                SwingUtilities.invokeLater(() -> {
                    mainWindow.updateFileResult(fileIndex, result);
                    statsWindow.updateStats(result);
                });
            }

            @Override
            public void onAllFinished(long totalTime) {
                sequentialTime = totalTime;
                SwingUtilities.invokeLater(() -> {
                    mainWindow.setSequentialTime(totalTime);
                    mainWindow.setStatus(AppStatus.DONE);
                    mainWindow.setButtonsEnabled(true); // Re-enable buttons
                    updateComparison();                 // Update speedup if both ran
                });
            }
        });

        // Concurrent listener — same structure, DRY
        concurrentCounter.setListener(new ConcurrentCounter.ConcurrentListener() {
            @Override
            public void onFileStarted(int fileIndex) {
                SwingUtilities.invokeLater(()
                        -> mainWindow.updateFileStatus(fileIndex, VowelResult.Status.PROCESSING)
                );
            }

            @Override
            public void onFileFinished(int fileIndex, VowelResult result) {
                SwingUtilities.invokeLater(() -> {
                    mainWindow.updateFileResult(fileIndex, result);
                    statsWindow.updateStats(result);
                });
            }

            @Override
            public void onAllFinished(long totalTime) {
                concurrentTime = totalTime;
                SwingUtilities.invokeLater(() -> {
                    mainWindow.setConcurrentTime(totalTime);
                    mainWindow.setStatus(AppStatus.DONE);
                    mainWindow.setButtonsEnabled(true);
                    updateComparison();
                });
            }
        });
    }

    // ── Called by btnUpload ───────────────────────────────────────
    public void loadFiles(List<File> files) {
        loadedFiles = files;
        lastResults.clear();

        // Reset UI for new files
        SwingUtilities.invokeLater(() -> {
            mainWindow.clearTable();
            mainWindow.setFileCount(files.size());
            statsWindow.clearStats();

            // Add each file to table as PENDING
            for (File file : files) {
                mainWindow.addFileRow(file.getName(), VowelResult.Status.PENDING);
            }
        });
    }

    // ── Called by btnSequential ───────────────────────────────────
    // Runs on a background thread — never block the EDT!
    public void runSequential() {
        if (loadedFiles.isEmpty()) {
            return;
        }

        // Disable buttons — no interruption allowed
        SwingUtilities.invokeLater(() -> {
            mainWindow.setButtonsEnabled(false);
            mainWindow.setStatus(AppStatus.RUNNING_SEQUENTIAL);
            mainWindow.resetFileStatuses(); // Back to PENDING
            statsWindow.clearStats();
        });

        // Run on background thread — sequential but NOT on Event Dispatch Thread 
        // If we ran on EDT, the UI would freeze completely
        //So just one thread in orden to UI don't die 
        new Thread(() -> {
            lastResults = sequentialCounter.process(loadedFiles);
        }, "Sequential-Runner").start();
    }

    // ── Called by btnConcurrent ───────────────────────────────────
    public void runConcurrent(int threadCount) {
        if (loadedFiles.isEmpty()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            mainWindow.setButtonsEnabled(false);
            mainWindow.setStatus(AppStatus.RUNNING_CONCURRENT);
            mainWindow.resetFileStatuses();
            statsWindow.clearStats();
            mainWindow.setThreadCount(threadCount);
        });

        // Run on background thread
        new Thread(() -> {
            lastResults = concurrentCounter.process(loadedFiles, threadCount);
        }, "Concurrent-Runner").start();
    }

    // ── Update comparison section — only if both times available ──
    private void updateComparison() {
        if (sequentialTime == 0 || concurrentTime == 0) {
            return;
        }

        TimeHelper.ComparisonResult comparison
                = TimeHelper.compare(sequentialTime, concurrentTime);

        SwingUtilities.invokeLater(() -> {
            mainWindow.setSpeedup(comparison.getSpeedupText());
            mainWindow.setEfficiency(comparison.getEfficiencyText());
        });
    }

    // ── Getters for UI ────────────────────────────────────────────
    public List<File> getLoadedFiles() {
        return loadedFiles;
    }

    public List<VowelResult> getLastResults() {
        return lastResults;
    }
}
