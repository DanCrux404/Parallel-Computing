package serverrmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author dante
 */
public class ServerRMI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            System.setProperty(
                    "java.rmi.server.hostname",
                    "192.168.0.160"
            );

            LocateRegistry.createRegistry(
                    Integer.parseInt(args[0]));

            MiInterfazRemota mir = new MiClaseRemota();

            java.rmi.Naming.rebind(
                    "//192.168.0.160:"
                    + args[0]
                    + "/PruebaRMI",
                    mir
            );

            System.out.println("Servidor RMI listo.");

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
