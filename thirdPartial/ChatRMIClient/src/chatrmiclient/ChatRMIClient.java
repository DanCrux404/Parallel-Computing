package chatrmiclient;

import GUI.ClientFrame;

/**
 *
 * @author dante
 */
public class ChatRMIClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        java.awt.EventQueue.invokeLater(() -> {
            ClientFrame frame = new ClientFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
