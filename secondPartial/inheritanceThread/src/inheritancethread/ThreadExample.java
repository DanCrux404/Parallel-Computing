package inheritancethread;

/**
 *
 * @author dante
 */
public class ThreadExample extends Thread {

    public ThreadExample(String str) {
        super(str);
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i + " " + getName());
        }
        System.out.println("Termina thread " + getName());
    }
}
