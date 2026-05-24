package basicconsumerproducer.models;

/**
 *
 * @author dante
 */
public class Consumer extends Thread {

    private Container container;

    public Consumer(Container c) {
        container = c;
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
        container.get();
        }
    }
}
