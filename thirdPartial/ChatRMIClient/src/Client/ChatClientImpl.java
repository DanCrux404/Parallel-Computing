package Client;

import Controller.ClientManager;
import Interfaces.ChatClient;
import Model.PeerInfo;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.Map;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    private ClientManager manager;

    public ChatClientImpl(ClientManager manager) throws RemoteException {
        this.manager = manager;
    }

    @Override
    public void receiveMessage(String username, String message) throws RemoteException {

        manager.receiveMessage(username, message);
    }

    @Override
    public void receivePrivateMessage(
            String username,
            String message
    )
            throws RemoteException {

        manager.receivePrivateMessage(
                username,
                message
        );
    }

    @Override
    public void updatePeers(
            Map<String, PeerInfo> peers
    )
            throws RemoteException {

        manager.updatePeers(peers);
    }
}
