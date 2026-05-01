package processing;

import Util.TimeHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import model.VowelResult;

/**
 *
 * @author dante
 */
// Processes files ONE BY ONE — no threads, pure sequential
// Used as baseline to compare against concurrent processing
public class SequentialCounter {
    // Listener to notify UI of progress — same pattern as previous projects

    public interface SequentialListener {

        void onFileStarted(int fileIndex);// File started processing

        void onFileFinished(int fileIndex, VowelResult result);// File done

        void onAllFinished(long totalTime);// All files done
    }

    private SequentialListener listener;

    public void setListener(SequentialListener listener) {
        this.listener = listener;
    }

    // == Main entry point — processes all files sequentially ===========
    // Returns list of results — one per file
    public List<VowelResult> process(List<File> files) {
        List<VowelResult> results = new java.util.ArrayList<>();

        // Start total timer — measures everything including overhead
        long totalStart = TimeHelper.startTimer();

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            VowelResult result = new VowelResult(file.getName());

            // Notify UI — this file is starting
            if (listener != null) {
                listener.onFileStarted(i);
            }
            result.setStatus(VowelResult.Status.PROCESSING);

            // Process the file and measure its individual time
            long fileStart = TimeHelper.startTimer();
            processFile(file, result);
            long fileTime = TimeHelper.stopTimer(fileStart, file.getName());

            result.setProcessingTime(fileTime);
            result.setStatus(VowelResult.Status.DONE);
            results.add(result);

            // Notify UI — this file is done
            if (listener != null) {
                listener.onFileFinished(i, result);
            }
        }

        // Stop total timer and notify UI
        long totalTime = TimeHelper.stopTimer(totalStart, "Sequential total");
        if (listener != null) {
            listener.onAllFinished(totalTime);
        }

        return results;
    }

    // == Processes a single file — reads char by char =================
    private void processFile(File file, VowelResult result) {
        // BufferedReader — efficient for large files, reads in chunks internally
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int c;
            // Read character by character — -1 means end of file
            while ((c = reader.read()) != -1) {
                result.countVowel((char) c);
            }
        } catch (IOException e) {
            // Mark as error — don't crash the whole process
            result.setStatus(VowelResult.Status.ERROR);
        }
    }
}
