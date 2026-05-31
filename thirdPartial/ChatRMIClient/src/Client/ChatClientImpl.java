package Client;

import Controller.ClientManager;
import Interfaces.ChatClient;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    private ClientManager manager;

    public ChatClientImpl(ClientManager manager) throws RemoteException {
        this.manager = manager;
    }

    @Override
    public void receiveMessage(String username, String message) throws RemoteException {

        manager.receiveMessage(username, message);
    }
}
