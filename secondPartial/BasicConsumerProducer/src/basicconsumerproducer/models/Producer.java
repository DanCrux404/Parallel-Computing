package basicconsumerproducer.models;

/**
 *
 * @author dante
 */
public class Producer extends Thread {

    private Container container;

    public Producer(Container c) {
        container = c;
    }

    public void run() {
        for (int i = 0; i < 10; i++) {
            container.put(i);
            try {
                sleep((int) (Math.random() * 100));
            } catch (InterruptedException e) {
            }
        }
    }
}