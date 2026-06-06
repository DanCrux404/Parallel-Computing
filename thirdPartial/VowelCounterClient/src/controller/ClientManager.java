package controller;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.SwingUtilities;
import model.VowelResult;
import rmi.VowelCounterImpl;
import common.ClientRegistry;
import windows.ClientWindow;

public class ClientManager {
    // Server connection constants — same values as RMIServer

    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "VowelCounter";

    private final ClientWindow window;
    private final String serverIp;
    private final String clientId;
    private VowelCounterImpl impl;

    public ClientManager(ClientWindow window, String serverIp, String clientId) {
        this.window = window;
        this.serverIp = serverIp;
        this.clientId = clientId;
    }

    public void connect() {
        SwingUtilities.invokeLater(() -> {
            window.setServerIp(serverIp);
            window.setStatus("Connecting...");
        });

        // Background thread — never block EDT
        new Thread(() -> {
            try {
                impl = new VowelCounterImpl();

                impl.setListener(new VowelCounterImpl.ClientListener() {
                    @Override
                    public void onProcessingStarted(int fileCount, int threadCount) {
                        SwingUtilities.invokeLater(() -> {
                            window.setStatus("Processing...");
                            window.setFileCount(fileCount);
                            window.setThreadCount(threadCount);
                            for (int i = 0; i < fileCount; i++) {
                                window.addFileRow("file_" + (i + 1));
                            }
                        });
                    }

                    public void onFileStarted(int fileIndex, String fileName) {
                        SwingUtilities.invokeLater(()
                                -> window.updateFileStatus(fileIndex,
                                        VowelResult.Status.PROCESSING, fileName)
                        );
                    }

                    @Override
                    public void onFileFinished(int fileIndex, VowelResult result) {
                        SwingUtilities.invokeLater(()
                                -> window.updateFileResult(fileIndex, result)
                        );
                    }

                    @Override
                    public void onAllFinished(long totalTime) {
                        SwingUtilities.invokeLater(() -> {
                            window.setStatus("Done ✓");
                            window.setProcessingTime(totalTime);
                        });
                    }
                });

                // Connect to server registry
                Registry registry = LocateRegistry.getRegistry(serverIp, RMI_PORT);
                ClientRegistry serverRegistry = (ClientRegistry) registry.lookup(SERVICE_NAME + "_registry");

                // Register ourselves — pass our impl so server can call us
                serverRegistry.register(impl, clientId);

                SwingUtilities.invokeLater(()
                        -> window.setStatus("Connected — waiting for files...")
                );

                System.out.println("Connected to " + serverIp + " as " + clientId);

                // Keep alive — server will call countFiles() when ready
                Thread.currentThread().join();

            } catch (Exception e) {
                System.out.println("Connection failed: " + e.getMessage());
                SwingUtilities.invokeLater(()
                        -> window.setStatus("Failed: " + e.getMessage())
                );
            }
        }, "Client-Connection").start();
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getClientId() {
        return clientId;
    }
}
