package clientsocket;

import java.io.IOException;

/**
 *
 * @author dante
 */
public class ClientSocket {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        System.out.println("Iniciando cliente...");
        client.startClient();
    }
}

