package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote Server Interface
 */
public interface ChatServer extends Remote {

    /**
     * Registers a new client
     *
     * @param client Client reference
     * @param username Client name
     * @throws RemoteException
     */
    void registerClient(String username, ChatClient client)
            throws RemoteException;

    /**
     * Sends a message to all connected clients
     *
     * @param username Sender username
     * @param message Message content
     * @throws RemoteException
     */
    void broadcastMessage(String username, String message)
            throws RemoteException;

    List<String> getConnectedUsers()
            throws RemoteException;
}
