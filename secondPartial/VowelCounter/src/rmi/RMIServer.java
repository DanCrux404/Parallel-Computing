package rmi;

import common.ClientRegistry;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import common.VowelCounterRemote;

// RMI Server — registers itself and waits for clients to connect
// Clients connect, server gives them files, they count, return results
public class RMIServer {

    // RMI port — must be the same on server and clients
    public static final int RMI_PORT = 1099; // default RMI port

    // Name clients use to find the server in the registry
    public static final String SERVICE_NAME = "VowelCounter";

    // Connected clients — added as they connect
    private final List<VowelCounterRemote> connectedClients = new ArrayList<>();

    // Listener to notify UI when a client connects/disconnects
    public interface ServerListener {

        void onClientConnected(int clientCount);

        void onClientDisconnected(int clientCount);

        void onReadyToProcess(int clientCount);
    }

    private ServerListener listener;

    public void setListener(ServerListener listener) {
        this.listener = listener;
    }

    // == Start RMI registry and wait for clients ==================
    public void start() throws Exception {
        // Create RMI registry on this machine
        // This is like the "phone book" — clients look up services here
        Registry registry = LocateRegistry.createRegistry(RMI_PORT);

        // Create the callback object — clients call this to register
        ClientRegistryImpl registryImpl = new ClientRegistryImpl(this);
        ClientRegistry stub = (ClientRegistry) UnicastRemoteObject.exportObject(registryImpl, 0);

        // Register in the registry — clients find it by SERVICE_NAME
        registry.rebind(SERVICE_NAME + "_registry", stub);

        System.out.println("RMI Server started on port " + RMI_PORT);
        System.out.println("Waiting for clients...");
    }

    // == Called when a client connects =======================
    public synchronized void registerClient(VowelCounterRemote client) {
        connectedClients.add(client);
        System.out.println("Client connected! Total: " + connectedClients.size());
        if (listener != null) {
            listener.onClientConnected(connectedClients.size());
        }
    }

    // == Called when a client disconnects ========================
    public synchronized void unregisterClient(VowelCounterRemote client) {
        connectedClients.remove(client);
        System.out.println("Client disconnected! Total: " + connectedClients.size());
        if (listener != null) {
            listener.onClientDisconnected(connectedClients.size());
        }
    }

    public synchronized List<VowelCounterRemote> getConnectedClients() {
        return new ArrayList<>(connectedClients); // defensive copy
    }

    public synchronized int getClientCount() {
        return connectedClients.size();
    }
}
