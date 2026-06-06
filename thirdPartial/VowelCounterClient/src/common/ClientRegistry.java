package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Interface clients use to register themselves with the server
// Client calls register() when it connects — passes itself as parameter
// Server stores the reference and calls countFiles() later
public interface ClientRegistry extends Remote {
    // Client calls this to say "I'm here, use me!"
    // client = the remote object server will call later
    // clientId = friendly name for logging

    void register(VowelCounterRemote client, String clientId)
            throws RemoteException;

    // Client calls this to say "I'm leaving"
    void unregister(VowelCounterRemote client) throws RemoteException;
}
