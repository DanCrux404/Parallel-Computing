package serverrmi;
import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 *
 * @author dante
 */
public interface MiInterfazRemota extends Remote{
    public void miMetodo1() throws RemoteException;
    public int miMetodo2() throws RemoteException;
}
