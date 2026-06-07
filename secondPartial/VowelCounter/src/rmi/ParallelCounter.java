package rmi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import common.VowelCounterRemote;
import model.VowelResult;
import processing.ConcurrentCounter;
import Util.TimeHelper;

public class ParallelCounter {

    public interface ParallelListener {

        void onPartitionAssigned(String nodeId, int fileCount);

        void onNodeFinished(String nodeId, VowelResult[] results);

        void onAllFinished(long totalTime);
    }

    private ParallelListener listener;
    private final RMIServer rmiServer;
    private final ConcurrentCounter serverCounter = new ConcurrentCounter();

    // Aumentado a 500 para reducir llamadas RMI con 3 nodos
    // 3333 archivos / 500 = ~7 llamadas por cliente (vs 33 con 100)
    private static final int BATCH_SIZE = 500;

    public ParallelCounter(RMIServer rmiServer) {
        this.rmiServer = rmiServer;
    }

    public void setListener(ParallelListener listener) {
        this.listener = listener;
    }

    public List<VowelResult> process(List<File> files, int threadsPerNode)
            throws Exception {

        List<VowelCounterRemote> clients = rmiServer.getConnectedClients();
        int totalNodes = clients.size() + 1; // +1 for server

        System.out.println("Parallel processing: " + totalNodes
                + " nodes, " + files.size() + " files");

        List<List<File>> partitions = partitionFiles(files, totalNodes);
        long totalStart = TimeHelper.startTimer();

        List<java.util.concurrent.Future<VowelResult[]>> clientFutures = new ArrayList<>();
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(
                clients.size() + 1);

        // === CLIENTS ===
        for (int i = 0; i < clients.size(); i++) {
            final VowelCounterRemote client = clients.get(i);
            final List<File> partition = partitions.get(i);
            final String clientId = "Client-" + (i + 1);
            final String clientBasePath = rmiServer.getClientBasePath(client);

            if (listener != null) {
                listener.onPartitionAssigned(clientId, partition.size());
            }

            clientFutures.add(pool.submit(() -> {
                List<VowelResult> clientResults = new ArrayList<>();
                int totalBatches = (int) Math.ceil(partition.size() / (double) BATCH_SIZE);

                for (int b = 0; b < partition.size(); b += BATCH_SIZE) {
                    List<File> batch = partition.subList(b,
                            Math.min(b + BATCH_SIZE, partition.size()));

                    // Usar solo el nombre del archivo — el cliente lo busca en Z:\
                    String[] batchPaths = batch.stream()
                            .map(f -> clientBasePath + f.getName())
                            .toArray(String[]::new);

                    VowelResult[] batchResults = client.countFiles(batchPaths, threadsPerNode);
                    clientResults.addAll(Arrays.asList(batchResults));

                    System.out.println(clientId + " batch "
                            + (b / BATCH_SIZE + 1) + "/" + totalBatches + " done");
                }

                VowelResult[] results = clientResults.toArray(VowelResult[]::new);
                if (listener != null) {
                    listener.onNodeFinished(clientId, results);
                }
                return results;
            }));
        }

        // === SERVER (última partición) ===
        List<File> serverPartition = partitions.get(partitions.size() - 1);

        if (listener != null) {
            listener.onPartitionAssigned("Server", serverPartition.size());
        }

        java.util.concurrent.Future<List<VowelResult>> serverFuture = pool.submit(() -> {
            List<VowelResult> results = new ArrayList<>();
            int totalBatches = (int) Math.ceil(serverPartition.size() / (double) BATCH_SIZE);

            for (int b = 0; b < serverPartition.size(); b += BATCH_SIZE) {
                List<File> batch = serverPartition.subList(b,
                        Math.min(b + BATCH_SIZE, serverPartition.size()));

                List<VowelResult> batchResults = serverCounter.process(batch, threadsPerNode);
                results.addAll(batchResults);

                System.out.println("Server -> batch "
                        + (b / BATCH_SIZE + 1) + "/" + totalBatches + " done");
            }

            if (listener != null) {
                listener.onNodeFinished("Server", results.toArray(new VowelResult[0]));
            }
            return results;
        });

        // === COLLECT RESULTS ===
        List<VowelResult> allResults = new ArrayList<>();

        for (java.util.concurrent.Future<VowelResult[]> future : clientFutures) {
            try {
                allResults.addAll(Arrays.asList(future.get()));
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Client error: " + e.getMessage());
            }
        }

        try {
            allResults.addAll(serverFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Server processing error: " + e.getMessage());
        }

        pool.shutdown();

        long totalTime = TimeHelper.stopTimer(totalStart, "Parallel total");

        if (listener != null) {
            listener.onAllFinished(totalTime);
        }

        return allResults;
    }

// Round-robin: file0->node0, file1->node1, file2->node2, file3->node0...
    private List<List<File>> partitionFiles(List<File> files, int nodeCount) {
        List<List<File>> partitions = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            partitions.add(new ArrayList<>());
        }
        for (int i = 0; i < files.size(); i++) {
            partitions.get(i % nodeCount).add(files.get(i));
        }

        // DEBUG: Print partition sizes
        for (int i = 0; i < partitions.size(); i++) {
            System.out.println("Partition " + i + ": " + partitions.get(i).size() + " files");
        }

        return partitions;
    }
}
