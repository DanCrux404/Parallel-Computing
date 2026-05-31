package Controller;

import Client.ChatClientImpl;
import GUI.ClientFrame;
import Interfaces.ChatServer;
import Interfaces.ChatClient;

import java.rmi.Naming;

public class ClientManager {

    private ClientFrame frame;
    private ChatServer server;
    private ChatClient client;

    private String username;

    public ClientManager(ClientFrame frame) {
        this.frame = frame;
    }

    public void connect(String host, int port) {

        try {

            username = frame.getUsername();

            if (username.isEmpty()) {
                frame.addMessage("Username cannot be empty");
                return;
            }

            server = (ChatServer) Naming.lookup(
                    "//" + host + ":" + port + "/ChatService"
            );

            try {
                client = new ChatClientImpl(this);
            } catch (Exception e) {

                frame.addMessage("Client init error: " + e.getMessage());
            }

            server.registerClient(username, client);

            frame.addMessage("Connected as " + username);

        } catch (Exception e) {

            frame.addMessage("Connection error: " + e.getMessage());
        }
    }

    public void sendMessage() {

        try {

            String message = frame.getMessage();

            if (message.isEmpty()) {
                return;
            }

            server.broadcastMessage(username, message);

            frame.clearMessage();

        } catch (Exception e) {

            frame.addMessage("Send error: " + e.getMessage());
        }
    }

    public void receiveMessage(String user, String message) {

        frame.addMessage(user + ": " + message);
    }

    public void updateUsers(java.util.List<String> users) {

        frame.updateUsers(users);
    }
}
