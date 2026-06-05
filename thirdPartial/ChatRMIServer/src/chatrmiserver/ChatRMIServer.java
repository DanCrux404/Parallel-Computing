package chatrmiserver;

import GUI.ServerFrame;

public class ChatRMIServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            java.awt.EventQueue.invokeLater(() -> {
                ServerFrame frame = new ServerFrame();

                frame.setLocationRelativeTo(null);

                frame.setVisible(true);
            });
        }catch(Exception e)
        {
            System.out.println("Error: " + e);
        }

    }
}
