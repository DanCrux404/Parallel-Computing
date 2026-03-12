package readerswriters;

import readerswriters.windows.MainWindow;

public class ReadersWriters {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        java.awt.EventQueue.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
        System.out.println("AAAAAAAAAAAAAA");
    }
    
}
