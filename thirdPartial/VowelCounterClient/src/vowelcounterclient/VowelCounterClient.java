package vowelcounterclient;

import windows.ClientWindow;
import controller.ClientManager;
import java.net.UnknownHostException;
import javax.swing.SwingUtilities;

public class VowelCounterClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String serverIp = args.length > 0 ? args[0] : null;
        String clientId = args.length > 1 ? args[1] : getHostName();

        if (serverIp == null || serverIp.isEmpty()) {
            serverIp = javax.swing.JOptionPane.showInputDialog(
                    null,
                    "Enter server IP address:",
                    "Connect to Server",
                    javax.swing.JOptionPane.QUESTION_MESSAGE
            );

            // User cancelled — exit
            if (serverIp == null || serverIp.trim().isEmpty()) {
                System.out.println("No IP provided — exiting");
                System.exit(0);
            }
        }

        final String finalIp = serverIp.trim();
        System.out.println("Starting client: " + clientId);
        System.out.println("Server: " + finalIp);

        SwingUtilities.invokeLater(() -> {
            ClientWindow window = new ClientWindow();
            window.setTitle("Vowel Counter Client — " + clientId);
            window.setVisible(true);
            ClientManager manager = new ClientManager(window, finalIp, clientId);
            manager.connect();
        });
    }

    private static String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown-Client";
        }
    }

}
