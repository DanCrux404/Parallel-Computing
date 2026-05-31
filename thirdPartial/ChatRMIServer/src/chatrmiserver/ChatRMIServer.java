package chatrmiserver;

import GUI.ServerFrame;

public class ChatRMIServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        java.awt.EventQueue.invokeLater(() -> {
            ServerFrame frame = new ServerFrame();
            
            frame.setLocationRelativeTo(null);
            
            frame.setVisible(true);
        });
    }
}
