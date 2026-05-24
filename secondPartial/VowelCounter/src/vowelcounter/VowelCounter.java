package vowelcounter;

import vowelcounter.windows.MainWindow;

/**
 *
 * @author dante
 */
public class VowelCounter {

    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Does anybody read this? O.o");

        java.awt.EventQueue.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}