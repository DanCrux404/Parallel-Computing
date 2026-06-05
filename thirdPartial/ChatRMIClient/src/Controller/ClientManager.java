package Controller;

import Client.ChatClientImpl;
import GUI.ClientFrame;
import Interfaces.ChatServer;
import Interfaces.ChatClient;
import Model.PeerInfo;
import java.awt.HeadlessException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

public class ClientManager {

    private ClientFrame frame;
    private ChatServer server;
    private ChatClient client;
    private String username;
    private Map<String, PeerInfo> peers;

    public ClientManager(ClientFrame frame) {
        this.frame = frame;
        peers = new HashMap<>();
    }

    public void connect(String host, int port) {

        try {

            username = frame.getUsername();

            if (username.isEmpty()) {

                frame.addMessage(
                        "Username cannot be empty"
                );

                return;
            }

            // Connect to central server
            server = (ChatServer) Naming.lookup(
                    "//" + host + ":" + port + "/ChatService"
            );

            // Create local remote object
            

            // Temporary port for this peer
            int peerPort = Integer.parseInt(
                    javax.swing.JOptionPane.showInputDialog(
                            frame,
                            "Peer Port:"
                    )
            );

            String ip = JOptionPane.showInputDialog(
                    frame,
                    "Your IP:"
            );

            System.setProperty(
                    "java.rmi.server.hostname",
                    ip
            );

            client = new ChatClientImpl(this);

            // Create local RMI Registry
            LocateRegistry.createRegistry(
                    peerPort
            );

            // Publish this client as a remote object
            Naming.rebind(
                    "//localhost:"
                    + peerPort
                    + "/"
                    + username,
                    client
            );

            // Send peer information to server
            PeerInfo peer
                    = new PeerInfo(
                            username,
                            ip,
                            peerPort
                    );

            server.registerClient(
                    peer,
                    client
            );

            Map<String, PeerInfo> peersFromServer
                    = server.getPeers();

            System.out.println(
                    "SERVER PEERS -> "
                    + peersFromServer.keySet()
            );

            updatePeers(
                    peersFromServer
            );

            frame.addMessage(
                    "Connected as "
                    + username
            );

        } catch (HeadlessException | NumberFormatException | MalformedURLException | NotBoundException | RemoteException ex) {
            System.getLogger(ClientManager.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public void sendMessage() throws MalformedURLException, NotBoundException {

        try {

            String message = frame.getMessage();

            if (message.isEmpty()) {
                return;
            }

            if (frame.isPrivateMessage()) {

                String targetUser = frame.getSelectedUser();

                PeerInfo targetInfo = peers.get(targetUser);

                System.out.println(
                        "TARGET HOST = "
                        + targetInfo.getHost()
                );

                System.out.println(
                        "TARGET PORT = "
                        + targetInfo.getPort()
                );

                ChatClient target
                        = (ChatClient) Naming.lookup(
                                "//"
                                + targetInfo.getHost()
                                + ":"
                                + targetInfo.getPort()
                                + "/"
                                + targetInfo.getUsername()
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

    //Dicctionary of each users to P2P comunication
    public void updatePeers(
            Map<String, PeerInfo> peers) {

        this.peers.clear();

        this.peers.putAll(peers);

        frame.updateUsers(
                new ArrayList<>(peers.keySet())
        );
    }
}
