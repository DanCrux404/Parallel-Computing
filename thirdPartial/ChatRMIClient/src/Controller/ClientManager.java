package Controller;

import Client.ChatClientImpl;
import GUI.ClientFrame;
import Interfaces.ChatServer;
import Interfaces.ChatClient;

import java.rmi.Naming;
import java.rmi.RemoteException;

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

            // DEBUG
            System.out.println(
                    server.getConnectedUsers()
            );

            frame.updateUsers(
                    server.getConnectedUsers()
            );

            frame.addMessage("Connected as " + username);

        } catch (Exception e) {

            frame.addMessage("Connection error: " + e.getMessage());
        }
    }

    public void sendMessage() {

        try {

            String message
                    = frame.getMessage();

            if (message.isEmpty()) {
                return;
            }

            if (frame.isPrivateMessage()) {

                String targetUser
                        = frame.getSelectedUser();

                ChatClient target
                        = server.getClient(
                                targetUser
                        );

                target.receivePrivateMessage(
                        username,
                        message
                );

                frame.addMessage(
                        "[TO "
                        + targetUser
                        + "] "
                        + message
                );

            } else {

                server.broadcastMessage(
                        username,
                        message
                );
            }

            frame.clearMessage();

        } catch (RemoteException e) {

            frame.addMessage(
                    "Error: "
                    + e.getMessage()
            );
        }
    }

    public void receiveMessage(String user, String message) {

        frame.addMessage(user + ": " + message);
    }

    public void updateUsers(java.util.List<String> users) {

        frame.updateUsers(users);
    }

    public void receivePrivateMessage(
            String username,
            String message
    ) {

        frame.addMessage(
                "[PRIVATE] "
                + username
                + ": "
                + message
        );
    }
}
