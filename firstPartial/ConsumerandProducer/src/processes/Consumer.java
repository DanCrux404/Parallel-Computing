package processes;

import model.Buffer;
import java.util.Random;

public class Consumer extends Thread {

    private final Buffer buffer;
    private final int id;
    // This variable can be read or written by multiple threads, no cache
    private volatile boolean running = true;
    private volatile ProcessState processState = ProcessState.RUNNING;

    private final int minSleep = 1000; // Consumer is slower than Producer
    private final int maxSleep = 2500;
    private final Random random = new Random();

    public enum ProcessState {
        RUNNING, SLEEPING, WAITING
    }

    // Callback so UI knows consumer state
    public interface ConsumerListener {
        void onStateChanged(int consumerId, ProcessState state);
    }

    private ConsumerListener listener;

    public Consumer(int id, Buffer buffer) {
        this.id = id;
        this.buffer = buffer;
        setName("Consumer-" + id); // Thread name — useful for debug
    }

    // The heart of the thread — executes when .start() is called
    @Override
    public void run() {
        while (running) {
            try {
                // Trying to consume — can block inside buffer.consume()
                setProcessState(ProcessState.RUNNING);

                // Critic section
                // Consumer cleans the slot (FIFO — always slot 0)
                // If buffer is empty, lambda sets state to WAITING
                // and the thread blocks inside buffer.consume()
                buffer.consume(() -> setProcessState(ProcessState.WAITING));
                // End critic section

                // Successful consume — rest
                setProcessState(ProcessState.SLEEPING);
                int sleepTime = minSleep + random.nextInt(maxSleep - minSleep);
                Thread.sleep(sleepTime); // Goes to sleep by its own choice

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Consumer-" + id + " stopped.");
    }

    private void setProcessState(ProcessState state) {
        this.processState = state;
        if (listener != null) {
            listener.onStateChanged(id, state);
        }
    }

    public void stopConsumer() {
        running = false;  // While ends naturally
        this.interrupt(); // If sleeping, wakes it with InterruptedException
    }

    public void setListener(ConsumerListener listener) {
        this.listener = listener;
    }

    public int getConsumerId()            { return id; }
    public ProcessState getProcessState() { return processState; }
}