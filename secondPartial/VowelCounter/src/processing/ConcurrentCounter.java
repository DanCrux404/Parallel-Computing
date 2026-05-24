package processing;

import Util.TimeHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import model.VowelResult;

/**
 *
 * @author dante
 */
// Processes files CONCURRENTLY using a thread pool
// N files processed simultaneously — N defined by user via spinner
// Uses ExecutorService + lambdas — clean and modern
public class ConcurrentCounter {
    // Same listener pattern as SequentialCounter — DRY in CounterManager

    public interface ConcurrentListener {

        void onFileStarted(int fileIndex);

        void onFileFinished(int fileIndex, VowelResult result);

        void onAllFinished(long totalTime);
    }

    private ConcurrentListener listener;

    public void setListener(ConcurrentListener listener) {
        this.listener = listener;
    }

    // == Main entry point — processes all files concurrently ==============
    public List<VowelResult> process(List<File> files, int threadCount) {
        List<VowelResult> results = new ArrayList<>();

        // Fixed thread pool — never more than threadCount threads active
        // Even if there are 1000 files, only threadCount run at once
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        // Future = a promise of a result — the thread will fill it later
        List<Future<VowelResult>> futures = new ArrayList<>();

        long totalStart = TimeHelper.startTimer();

        // Submit one task per file — pool decides when each runs
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            int fileIndex = i; // Must be effectively final for lambda

            // Notify UI — file is queued
            VowelResult result = new VowelResult(file.getName());
            results.add(result); // Add now to preserve order

            // Submit task as lambda — this is where lambdas shine!!!!
            Future<VowelResult> future = pool.submit(() -> {
                // Notify UI — file started
                if (listener != null) {
                    listener.onFileStarted(fileIndex);
                }
                result.setStatus(VowelResult.Status.PROCESSING);

                // Time each file silently — no console contention
                long fileStart = TimeHelper.startTimer();
                processFile(file, result);
                long fileTime = TimeHelper.stopTimerSilent(fileStart);

                result.setProcessingTime(fileTime);
                result.setStatus(VowelResult.Status.DONE);

                // Notify UI — file done
                if (listener != null) {
                    listener.onFileFinished(fileIndex, result);
                }

                return result;
            });

            futures.add(future);
        }

        // Wait for ALL futures to complete before measuring total time
        // get() blocks until that specific task is done
        for (Future<VowelResult> future : futures) {
            try {
                future.get(); // Blocks until this file is done
            } catch (Exception e) {
                // Don't crash — one file failing shouldn't stop the rest
                System.out.println("Error processing file: " + e.getMessage());
            }
        }

        // All done — stop total timer
        long totalTime = TimeHelper.stopTimer(totalStart, "Concurrent total");

        // Shutdown pool — release threads, no more tasks accepted
        pool.shutdown();

        if (listener != null) {
            listener.onAllFinished(totalTime);
        }

        return results;
    }

    // == Processes a single file — same as SequentialCounter =======
    // Not synchronized — each thread works on its OWN file
    // No shared data between threads here
    private void processFile(File file, VowelResult result) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int c;
            while ((c = reader.read()) != -1) {
                result.countVowel((char) c);
            }
        } catch (IOException e) {
            result.setStatus(VowelResult.Status.ERROR);
        }
    }
}
