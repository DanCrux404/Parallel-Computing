package consumerandproducer;
import windows.MainWindow;
/**
 *
 * @author dante
 */
public class ConsumerandProducer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
        System.out.println("Hello world, I hate Java and netbeans. HELP MEEEE");
    }
    
}
