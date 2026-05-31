package Controller;

import GUI.ServerFrame;

public class ServerManager {

    private final ServerFrame serverFrame;

    public ServerManager(
            ServerFrame serverFrame
    ) {

        this.serverFrame = serverFrame;
    }

    public void logMessage(
            String message
    ) {

        serverFrame.addLog(message);
    }

    public void updateClientCount(
            int count
    ) {

        serverFrame.updateClients(count);
    }
}
