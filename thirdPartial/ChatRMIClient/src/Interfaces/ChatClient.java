package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote Client Interface
 */
public interface ChatClient extends Remote {

    /**
     * Receives a message from the server
     *
     * @param username Sender username
     * @param message Message content
     * @throws RemoteException
     */
    void receiveMessage(String username, String message)
            throws RemoteException;

    void receivePrivateMessage(
            String username,
            String message
    )
            throws RemoteException;

    void updateUserList(
            List<String> users
    )
            throws RemoteException;
}
