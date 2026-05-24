package clientrmi;

import java.rmi.Naming;
import serverrmi.MiInterfazRemota;
/**
 *
 * @author dante
 */
public class ClientRMI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            MiInterfazRemota mir
                    = (MiInterfazRemota) Naming.lookup("//"
                            + args[0] + ":" + args[1] + "/PruebaRMI");

// Imprimimos miMetodo1() tantas veces como devuelva miMetodo2()
            for (int i = 1; i <= mir.miMetodo2(); i++) {
                mir.miMetodo1();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
