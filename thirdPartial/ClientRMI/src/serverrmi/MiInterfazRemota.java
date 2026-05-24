package serverrmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MiInterfazRemota extends Remote {
    void miMetodo1() throws RemoteException;
    int miMetodo2() throws RemoteException;
}