package clientsocket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author dante
 */
public class Client {

    protected Socket serverSocket;
    protected DataOutputStream outputServer;

    public Client() throws IOException {
        serverSocket = new Socket("192.168.0.160", 1234);
    }

    public void startClient() {
        try {
//Flujo de datos hacia el servidor
            outputServer = new DataOutputStream(serverSocket.getOutputStream());
            for (int i = 0; i < 5; i++) {
                outputServer.writeUTF("Este es el mensaje número " + (i + 1) + "\n");
            }
            serverSocket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
