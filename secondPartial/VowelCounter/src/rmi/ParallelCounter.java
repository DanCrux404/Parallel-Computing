package rmi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import common.VowelCounterRemote;
import model.VowelResult;
import processing.ConcurrentCounter;
import Util.TimeHelper;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

// Orchestrates parallel processing across server + connected clients
// Divides files, sends to clients, server processes its own portion
// Minimal communication — one RMI call per client, all files at once
public class ParallelCounter {

    // Listener to notify UI of progress
    public interface ParallelListener {

        void onPartitionAssigned(String nodeId, int fileCount);

        void onNodeFinished(String nodeId, VowelResult[] results);

        void onAllFinished(long totalTime);
    }

    private ParallelListener listener;
    private final RMIServer rmiServer;

    // Server also counts using its own ConcurrentCounter
    // Server is just another node — not special
    private final ConcurrentCounter serverCounter = new ConcurrentCounter();

    public ParallelCounter(RMIServer rmiServer) {
        this.rmiServer = rmiServer;
    }

    public void setListener(ParallelListener listener) {
        this.listener = listener;
    }

    // == Main entry point = divides and conquers ==================
    public List<VowelResult> process(List<File> files, int threadsPerNode)
            throws Exception {

        List<VowelCounterRemote> clients = rmiServer.getConnectedClients();
        int totalNodes = clients.size() + 1; // clients + server itself

        System.out.println("Parallel processing: " + totalNodes
                + " nodes, " + files.size() + " files");

        // == Divide files between all nodes ====================
        // Each node gets approximately files.size() / totalNodes files
        List<List<File>> partitions = partitionFiles(files, totalNodes);

        long totalStart = TimeHelper.startTimer();

        // == Submit tasks to clients via RMI ===================
        // Use Future pattern — submit all, then collect results
        List<java.util.concurrent.Future<VowelResult[]>> clientFutures
                = new ArrayList<>();

        java.util.concurrent.ExecutorService pool
                = java.util.concurrent.Executors.newFixedThreadPool(clients.size());

        for (int i = 0; i < clients.size(); i++) {
            final VowelCounterRemote client = clients.get(i);
            final List<File> partition = partitions.get(i); // client gets partition i
            final String clientId = "Client-" + (i + 1);

            if (listener != null) {
                listener.onPartitionAssigned(clientId, partition.size());
            }

            // Submit RMI call as async task — don't block waiting for each client
            // All clients work in parallel 
            clientFutures.add(pool.submit(() -> {
                // Read file bytes — server reads, sends to client
                byte[][] filesContent = readFilesAsBytes(partition);
                String[] fileNames = partition.stream()
                        .map(File::getName)
                        .toArray(String[]::new);
                
                // ONE RMI call — minimal communication
                VowelResult[] results;
                results = client.countFiles(filesContent, fileNames, threadsPerNode);
                
                if (listener != null) {
                    listener.onNodeFinished(clientId, results);
                }
                return results;
            }));
        }

        // = Server processes its own partition concurrently ==========
        // Server doesn't sit idle while clients work
        List<File> serverPartition = partitions.get(partitions.size() - 1);

        if (listener != null) {
            listener.onPartitionAssigned("Server", serverPartition.size());
        }

        // Server uses ConcurrentCounter — same as second parcial
        java.util.concurrent.Future<List<VowelResult>> serverFuture;
        serverFuture = pool.submit(() -> {
            List<VowelResult> results = serverCounter.process(
                    serverPartition, threadsPerNode
            );
            if (listener != null) {
                listener.onNodeFinished("Server",
                        results.toArray(VowelResult[]::new));
            }
            return results;
        });

        // == Collect ALL results ==============================
        // Wait for everyone — server and all clients
        List<VowelResult> allResults = new ArrayList<>();

        // Collect client results
        for (java.util.concurrent.Future<VowelResult[]> future : clientFutures) {
            try {
                VowelResult[] clientResults = future.get();
                allResults.addAll(Arrays.asList(clientResults));
            } catch (InterruptedException | ExecutionException e) {
                // Client failed — log but don't crash everything
                System.out.println("Client error: " + e.getMessage());
            }
        }

        // Collect server results
        try {
            allResults.addAll(serverFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Server processing error: " + e.getMessage());
        }

        pool.shutdown();

        // == Total time — from first file sent to last result received ==
        long totalTime = TimeHelper.stopTimer(totalStart, "Parallel total");

        if (listener != null) {
            listener.onAllFinished(totalTime);
        }

        return allResults;
    }

    // == Divide files as evenly as possible between nodes =========
    // Example: 9 files, 3 nodes → [3, 3, 3]
    // Example: 10 files, 3 nodes → [4, 3, 3]
    private List<List<File>> partitionFiles(List<File> files, int nodeCount) {
        List<List<File>> partitions = new ArrayList<>();

        // Initialize empty partitions
        for (int i = 0; i < nodeCount; i++) {
            partitions.add(new ArrayList<>());
        }

        // Round-robin distribution — spreads files evenly
        // file0 → node0, file1 → node1, file2 → node2, file3 → node0...
        /*
            i % nodeCount:

            file0    → 0 % 3 = 0 → node0
            file1    → 1 % 3 = 1 → node1
            file2    → 2 % 3 = 2 → node2
            file3    → 3 % 3 = 0 → node0 
            file4    → 4 % 3 = 1 → node1
            file5    → 5 % 3 = 2 → node2
            file6    → 6 % 3 = 0 → node0
            ...
            file9999 → 9999 % 3 = 0 → node0

            // Con 10,000 archivos y 3 nodos:
            // node0 → 3334 archivos
            // node1 → 3333 archivos
            // node2 → 3333 archivos
            // Max diff of 1 file
         */
        for (int i = 0; i < files.size(); i++) {
            partitions.get(i % nodeCount).add(files.get(i));
        }

        return partitions;
    }

    // == Read file content as bytes — for sending via RMI ========
    private byte[][] readFilesAsBytes(List<File> files) throws IOException {
        byte[][] contents = new byte[files.size()][];
        for (int i = 0; i < files.size(); i++) {
            contents[i] = Files.readAllBytes(files.get(i).toPath());
        }
        return contents;
    }
}
