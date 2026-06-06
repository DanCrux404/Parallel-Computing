package rmi;

import common.ClientRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import common.VowelCounterRemote;

// Implementation of ClientRegistry — runs on the server
// When a client calls register(), this stores the client reference
public class ClientRegistryImpl extends UnicastRemoteObject
        implements ClientRegistry {

    private final RMIServer server;

    public ClientRegistryImpl(RMIServer server) throws RemoteException {
        super();
        this.server = server;
    }

    @Override
    public void register(VowelCounterRemote client, String clientId)
            throws RemoteException {
        System.out.println("Client registering: " + clientId);
        server.registerClient(client);
    }

    @Override
    public void unregister(VowelCounterRemote client) throws RemoteException {
        server.unregisterClient(client);
    }
}
