package Interfaces;

import Model.PeerInfo;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Remote Server Interface
 */
public interface ChatServer extends Remote {

    /**
     * Registers a new client
     *
     * @param peer info for a peer
     * @throws RemoteException
     */
    void registerClient(
            PeerInfo peer,
            ChatClient client
    )
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

    Map<String, PeerInfo> getPeers()
            throws RemoteException;

}
