package serversocket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author dante
 */
public class Server {

    protected ServerSocket serverSocket;
    protected Socket clientSocket;
    protected DataOutputStream outputClient;
    protected BufferedReader input;
    protected String menssage;

    public Server() throws IOException {
        serverSocket = new ServerSocket(1234);
        clientSocket = new Socket();
    }

    public void startServer() {
        try {
            while (true) {
                System.out.println("Esperando...");

                clientSocket = serverSocket.accept();
                System.out.println("Cliente en línea...");

                outputClient = new DataOutputStream(
                        clientSocket.getOutputStream()
                );

                outputClient.writeUTF("Petición recibida y aceptada");

                input = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()
                        )
                );

                while ((menssage = input.readLine()) != null) {
                    System.out.println(menssage);
                }

                System.out.println("Fin de la conexión");

                clientSocket.close();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
