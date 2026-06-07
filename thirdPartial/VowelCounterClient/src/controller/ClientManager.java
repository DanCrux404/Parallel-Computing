package controller;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.SwingUtilities;
import model.VowelResult;
import rmi.VowelCounterImpl;
import common.ClientRegistry;
import java.io.IOException;
import windows.ClientWindow;

public class ClientManager {

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
                            window.clearTable(); // Clear previous run
                        });
                    }

                    @Override
                    public void onFileStarted(int fileIndex, String fileName) {
                        SwingUtilities.invokeLater(() -> {
                            // Add row with real filename, then mark as processing
                            window.addFileRow(fileName);
                            window.updateFileStatus(fileIndex,
                                    VowelResult.Status.PROCESSING, fileName);
                        });
                    }

                    @Override
                    public void onFileFinished(int fileIndex, VowelResult result) {
                        SwingUtilities.invokeLater(() -> {
                            window.updateFileResult(fileIndex, result);
                        });
                    }

                    @Override
                    public void onAllFinished(long totalTime) {
                        SwingUtilities.invokeLater(() -> {
                            window.setStatus("Done");
                            window.setProcessingTime(totalTime);
                        });
                    }
                });

                System.setProperty("java.rmi.server.hostname", getRealIp());
                // Connect to server registry
                Registry registry = LocateRegistry.getRegistry(serverIp, RMI_PORT);
                ClientRegistry serverRegistry = (ClientRegistry) registry.lookup(SERVICE_NAME + "_registry");

                // Register ourselves — pass our impl so server can call us
                String basePath = "C:\\TestFilesLocal\\";//Local files

                serverRegistry.register(impl, clientId, basePath);
                System.out.println("Registered with basePath: " + basePath);

                SwingUtilities.invokeLater(() -> {
                    window.setStatus("Connected — waiting for files...");
                });

                System.out.println("Connected to " + serverIp + " as " + clientId);

                // Keep alive — server will call countFiles() when ready
                Thread.currentThread().join();

            } catch (Exception e) {
                System.out.println("Connection failed: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    window.setStatus("Failed: " + e.getMessage());
                });
            }
        }, "Client-Connection").start();
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getClientId() {
        return clientId;
    }

    // Get real network IP — avoids 127.0.1.1 issue on Debian/Linux
    private String getRealIp() {
        try {
            String ip;
            try ( // Connect to server IP to find which local interface we use
                    java.net.Socket socket = new java.net.Socket(serverIp, RMI_PORT)) {
                ip = socket.getLocalAddress().getHostAddress();
            }
            return ip;
        } catch (IOException e) {
            // Fallback — try network interfaces directly
            try {
                java.util.Enumeration<java.net.NetworkInterface> interfaces
                        = java.net.NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    java.net.NetworkInterface iface = interfaces.nextElement();
                    if (iface.isLoopback() || !iface.isUp()) {
                        continue;
                    }
                    java.util.Enumeration<java.net.InetAddress> addresses
                            = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress addr = addresses.nextElement();
                        if (addr instanceof java.net.Inet4Address) {
                            return addr.getHostAddress();
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Could not determine IP: " + ex.getMessage());
            }
            return "localhost";
        }
    }
}
