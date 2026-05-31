package Server;

import Interfaces.ChatClient;
import Interfaces.ChatServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import Controller.ServerManager;

/**
 * Chat Server Implementation UnicastRemoteObject it is used to export a remote
 * object making it accessible through the network using JRMP (Java Remote
 * Method Protocol) protocol.
 */
public class ChatServerImpl extends UnicastRemoteObject
        implements ChatServer {

    private Map<String, ChatClient> clients;
    private ServerManager manager;

    public ChatServerImpl(
            ServerManager manager
    ) throws RemoteException {

        super();

        this.manager = manager;

        clients = new HashMap<>();
    }

    @Override
    public void registerClient(
            String username,
            ChatClient client
    ) throws RemoteException {

        clients.put(
                username,
                client
        );

        manager.logMessage(
                username
                + " connected."
        );

        manager.updateClientCount(
                clients.size()
        );
    }

    @Override
    public void broadcastMessage(String username,
            String message)
            throws RemoteException {

        manager.logMessage(
                "[" + username + "] "
                + message
        );

        for (ChatClient client : clients.values()) {

            try {

                client.receiveMessage(
                        username,
                        message
                );

            } catch (Exception e) {

                System.out.println(
                        "Error Sending Message: "
                        + e.getMessage()
                );
            }
        }
    }
}
