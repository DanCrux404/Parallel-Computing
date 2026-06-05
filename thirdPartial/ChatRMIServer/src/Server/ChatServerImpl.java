package Server;

import Interfaces.ChatClient;
import Interfaces.ChatServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Naming;
import java.util.HashMap;
import java.util.Map;
import Controller.ServerManager;
import Model.PeerInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat Server Implementation UnicastRemoteObject it is used to export a remote
 * object making it accessible through the network using JRMP (Java Remote
 * Method Protocol) protocol.
 */
public class ChatServerImpl extends UnicastRemoteObject
        implements ChatServer {

    private Map<String, ChatClient> clients;
    private Map<String, PeerInfo> peers;
    private ServerManager manager;

    public ChatServerImpl(
            ServerManager manager
    ) throws RemoteException {

        super();

        this.manager = manager;
        clients = new HashMap<>();
        peers = new HashMap<>();
    }

    @Override
    public void registerClient(
            PeerInfo peer,
            ChatClient client
    )
            throws RemoteException {

        peers.put(
                peer.getUsername(),
                peer
        );

        clients.put(
                peer.getUsername(),
                client
        );

        for (ChatClient c : clients.values()) {

            try {

                c.updatePeers(
                        new HashMap<>(peers)
                );

            } catch (RemoteException e) {

                System.out.println(
                        e.getMessage()
                );
            }
        }

        manager.logMessage(
                peer.getUsername()
                + " connected."
        );

        manager.updateClientCount(
                peers.size()
        );
    }

    @Override
    public void broadcastMessage(
            String username,
            String message
    ) throws RemoteException {

        manager.logMessage(
                "[" + username + "] "
                + message
        );

        for (PeerInfo peer : peers.values()) {

            try {

                ChatClient client
                        = (ChatClient) Naming.lookup(
                                "//"
                                + peer.getHost()
                                + ":"
                                + peer.getPort()
                                + "/"
                                + peer.getUsername()
                        );

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

    @Override
    public List<String> getConnectedUsers()
            throws RemoteException {

        return new ArrayList<>(
                peers.keySet()
        );
    }

    @Override
    public Map<String, PeerInfo> getPeers()
            throws RemoteException {

        return new HashMap<>(peers);
    }
}
