package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Interface clients use to register themselves with the server
// Client calls register() when it connects — passes itself as parameter
// Server stores the reference and calls countFiles() later
public interface ClientRegistry extends Remote {
    void register(VowelCounterRemote client, 
                  String clientId,
                  String basePath)  // ← agregar
        throws RemoteException;
    void unregister(VowelCounterRemote client) throws RemoteException;
}
