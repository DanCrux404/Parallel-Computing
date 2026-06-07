package rmi;

import common.VowelCounterRemote;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import model.VowelResult;

// Implementation of VowelCounterRemote — runs on the CLIENT
// Server calls countFiles() remotely — client processes with its own threads
// Client is autonomous — server doesn't care HOW it counts, just the results
public class VowelCounterImpl extends UnicastRemoteObject
        implements VowelCounterRemote {

    // Listener to notify ClientWindow of progress
    public interface ClientListener {

        void onProcessingStarted(int fileCount, int threadCount);

        void onFileFinished(int fileIndex, VowelResult result);

        void onAllFinished(long totalTime);
        
        void onFileStarted(int fileIndex, String fileName);
    }

    private ClientListener listener;

    public VowelCounterImpl() throws RemoteException {
        super(); // Required by UnicastRemoteObject
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    // == Called by server via RMI — process all files concurrently ==
    @Override
    public VowelResult[] countFiles(String[] filePaths, int threadsPerNode)
            throws RemoteException {

        int fileCount = filePaths.length;
        System.out.println("Received " + fileCount + " file paths, "
                + "using " + threadsPerNode + " threads");

        if (listener != null) {
            listener.onProcessingStarted(fileCount, threadsPerNode);
        }

        ExecutorService pool = Executors.newFixedThreadPool(threadsPerNode);
        List<Future<VowelResult>> futures = new ArrayList<>();
        long startTime = System.nanoTime();

        for (int i = 0; i < fileCount; i++) {
            final String filePath = filePaths[i];
            final int fileIndex = i;
            final String fileName = new File(filePath).getName();

            futures.add(pool.submit(() -> {
                VowelResult result = new VowelResult(fileName);
                result.setStatus(VowelResult.Status.PROCESSING);

                if (listener != null) {
                    listener.onFileStarted(fileIndex, fileName);
                }

                long fileStart = System.nanoTime();

                // Read file LOCALLY — no network transfer needed ✅
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.FileReader(filePath))) {
                    int c;
                    while ((c = reader.read()) != -1) {
                        result.countVowel((char) c);
                    }
                } catch (Exception e) {
                    result.setStatus(VowelResult.Status.ERROR);
                }

                long fileTime = (System.nanoTime() - fileStart) / 1_000_000;
                result.setProcessingTime(fileTime);
                result.setStatus(VowelResult.Status.DONE);

                if (listener != null) {
                    listener.onFileFinished(fileIndex, result);
                }
                return result;
            }));
        }

        VowelResult[] results = new VowelResult[fileCount];
        for (int i = 0; i < futures.size(); i++) {
            try {
                results[i] = futures.get(i).get();
            } catch (InterruptedException | ExecutionException e) {
                results[i] = new VowelResult(new File(filePaths[i]).getName());
                results[i].setStatus(VowelResult.Status.ERROR);
            }
        }

        pool.shutdown();
        long totalTime = (System.nanoTime() - startTime) / 1_000_000;

        if (listener != null) {
            listener.onAllFinished(totalTime);
        }

        return results;
    }

    // == ping — server checks if client is alive =============0
    @Override
    public boolean ping() throws RemoteException {
        return true; // If we got here, we're alive 
    }

    // == Count vowels from raw bytes — no file needed ==============
    // Same logic as SequentialCounter but reads from bytes not file
    // Bytes travel via RMI through server
    private void countVowelsFromBytes(byte[] content, VowelResult result) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ByteArrayInputStream(content), "UTF-8"))) {
            int c;
            while ((c = reader.read()) != -1) {
                result.countVowel((char) c);
            }
        } catch (Exception e) {
            result.setStatus(VowelResult.Status.ERROR);
        }
    }
}
