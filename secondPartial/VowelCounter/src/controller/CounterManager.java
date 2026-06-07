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
import rmi.ParallelCounter;
import rmi.RMIServer;
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
    private long parallelTime = 0;

    // Counters reused across runs
    private final SequentialCounter sequentialCounter = new SequentialCounter();
    private final ConcurrentCounter concurrentCounter = new ConcurrentCounter();

    //Counters for parallel
    private final RMIServer rmiServer = new RMIServer();
    private final ParallelCounter parallelCounter = new ParallelCounter(rmiServer);

    private java.util.Map<String, Integer> fileIndexMap = new java.util.HashMap<>();

    public CounterManager(MainWindow mainWindow, StatsWindow statsWindow) {
        this.mainWindow = mainWindow;
        this.statsWindow = statsWindow;

        setupListeners();

// Start RMI server — wait for clients
        try {
            rmiServer.setListener(new RMIServer.ServerListener() {
                @Override
                public void onClientConnected(int count) {
                    SwingUtilities.invokeLater(()
                            -> mainWindow.setStatus(AppStatus.IDLE)
                    );
                    System.out.println("Clients connected: " + count);
                }

                @Override
                public void onClientDisconnected(int count) {
                    System.out.println("Clients connected: " + count);
                }

                @Override
                public void onReadyToProcess(int count) {
                }
            });
            rmiServer.start();
        } catch (Exception e) {
            System.out.println("RMI Server failed: " + e.getMessage());
        }

        // Setup parallel listener
        parallelCounter.setListener(new ParallelCounter.ParallelListener() {
            @Override
            public void onPartitionAssigned(String nodeId, int fileCount) {
                System.out.println(nodeId + " -> " + fileCount + " files");
            }

            @Override
            public void onNodeFinished(String nodeId, VowelResult[] results) {
                SwingUtilities.invokeLater(() -> {
                    for (VowelResult r : results) {
                        statsWindow.updateStats(r);
                    }
                });
            }

            @Override
            public void onAllFinished(long totalTime) {
                SwingUtilities.invokeLater(() -> {
                    mainWindow.setParallelTime(totalTime);
                    mainWindow.setStatus(AppStatus.DONE);
                    mainWindow.setButtonsEnabled(true);
                    updateComparison();
                });
            }
        });
    }

    public void runParallel(int threadsPerNode) {
        if (loadedFiles.isEmpty()) {
            return;
        }

        int clientCount = rmiServer.getClientCount();
        System.out.println("Parallel with " + (clientCount + 1) + " nodes");

        SwingUtilities.invokeLater(() -> {
            mainWindow.setButtonsEnabled(false);
            mainWindow.setStatus(AppStatus.RUNNING_PARALLEL); // ← Agrega esto al enum
            mainWindow.resetFileStatuses();
            statsWindow.clearStats();
            mainWindow.setThreadCount(threadsPerNode);
        });

        new Thread(() -> {
            try {
                List<VowelResult> results = parallelCounter.process(loadedFiles, threadsPerNode);

                // Update table with ALL results at the end
                SwingUtilities.invokeLater(() -> {
                    for (VowelResult r : results) {
                        int fileIndex = findFileIndex(r.getFileName());
                        if (fileIndex >= 0) {
                            mainWindow.updateFileResult(fileIndex, r);
                        }
                    }

                    mainWindow.setStatus(AppStatus.DONE);
                    mainWindow.setButtonsEnabled(true);
                });

            } catch (Exception e) {
                System.out.println("Parallel error: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    mainWindow.setStatus(AppStatus.DONE);
                    mainWindow.setButtonsEnabled(true);
                });
            }
        }, "Parallel-Runner").start();
    }

    public void loadFiles(List<File> files) {
        loadedFiles = files;
        fileIndexMap.clear(); // Reset map
        lastResults.clear();

        // Build index map for O(1) lookups
        for (int i = 0; i < files.size(); i++) {
            fileIndexMap.put(files.get(i).getName(), i);
        }

        SwingUtilities.invokeLater(() -> {
            mainWindow.clearTable();
            mainWindow.setFileCount(files.size());
            statsWindow.clearStats();

            for (File file : files) {
                mainWindow.addFileRow(file.getName(), VowelResult.Status.PENDING);
            }
        });
    }

// Replace findFileIndex with O(1) lookup
    private int findFileIndex(String fileName) {
        Integer index = fileIndexMap.get(fileName);
        return index != null ? index : -1;
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

    // == Called by btnSequential =============================
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

    // == Called by btnConcurrent ======================
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

    // == Update comparison section only if both times available ==
    private void updateComparison() {
        if (sequentialTime > 0 && concurrentTime > 0) {
            TimeHelper.ComparisonResult c
                    = TimeHelper.compare(sequentialTime, concurrentTime);
            mainWindow.setSpeedup(c.getSpeedupText());
            mainWindow.setEfficiency(c.getEfficiencyText());
        }
        // También comparar parallel vs sequential
        if (sequentialTime > 0 && parallelTime > 0) {
            TimeHelper.ComparisonResult p
                    = TimeHelper.compare(sequentialTime, parallelTime);
            mainWindow.setParallelSpeedup(p.getSpeedupText());
        }
    }

    // == Getters for UI ================================
    public List<File> getLoadedFiles() {
        return loadedFiles;
    }

    public List<VowelResult> getLastResults() {
        return lastResults;
    }
}
