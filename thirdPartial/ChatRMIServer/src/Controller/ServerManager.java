package Controller;

import GUI.ServerFrame;

// I think now we all know what this class does...
// Same pattern as SimulationManager, vowelCounter... in previous projects
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
