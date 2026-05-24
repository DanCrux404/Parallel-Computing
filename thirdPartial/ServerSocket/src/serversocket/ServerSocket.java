package serversocket;

import java.io.IOException;

/**
 *
 * @author dante
 */
public class ServerSocket {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        System.out.println("Iniciando servidor...");
        server.startServer();
    }

}
