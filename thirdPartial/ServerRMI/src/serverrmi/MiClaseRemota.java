package serverrmi;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

/**
 *
 * @author dante
 */
public class MiClaseRemota extends UnicastRemoteObject implements
        MiInterfazRemota {

    public MiClaseRemota() throws RemoteException {
    // Código del constructor
    }

    @Override
    public void miMetodo1() throws RemoteException {
    // Aquí ponemos el código que queramos
        System.out.println("Estoy en miMetodo1()");
    }

    @Override
    public int miMetodo2() throws RemoteException {
    // Aquí ponemos el código que queramos
        return 5;
    }

    public void otroMetodo() {
//
//
    }
}
