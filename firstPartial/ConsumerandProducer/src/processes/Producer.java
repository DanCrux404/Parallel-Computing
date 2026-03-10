package processes;

import java.awt.Color;
import java.util.Random;
import model.Buffer;

public class Producer extends Thread {

    private final Buffer buffer;
    private final Color color;
    private final int id;
    // This variable can be read or written by multiple threads, no cache
    private volatile boolean running = true;
    private volatile ProcessState processState = ProcessState.RUNNING;

    private final int minSleep = 500;  // Minimum ms between productions
    private final int maxSleep = 1500; // Maximum ms between productions
    private final Random random = new Random();

    public enum ProcessState {
        RUNNING, SLEEPING, WAITING
    }

    // Callback so UI knows producer state
    public interface ProducerListener {
        void onStateChanged(int producerId, ProcessState state);
    }

    private ProducerListener listener;

    public Producer(int id, Buffer buffer, Color color) {
        this.id = id;
        this.buffer = buffer;
        this.color = color;
        setName("Producer-" + id); // Thread name — useful for debug
    }

    // The heart of the thread — executes when .start() is called
    @Override
    public void run() {
        while (running) {
            try {
                // Trying to produce — can block inside buffer.produce()
                setProcessState(ProcessState.RUNNING);

                // Critic section
                // If buffer is full, the lambda sets state to WAITING
                // and the thread blocks inside buffer.produce()
                buffer.produce(color, () -> setProcessState(ProcessState.WAITING));
                // End critic section

                // Successful production — rest before next
                setProcessState(ProcessState.SLEEPING);
                int sleepTime = minSleep + random.nextInt(maxSleep - minSleep);
                Thread.sleep(sleepTime); // Goes to sleep by its own choice

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Producer-" + id + " stopped.");
    }

    private void setProcessState(ProcessState state) {
        this.processState = state;
        if (listener != null) {
            listener.onStateChanged(id, state);
        }
    }

    public void stopProducer() {
        running = false;      // While ends naturally
        this.interrupt();     // If sleeping, wakes it with InterruptedException
    }

    public void setListener(ProducerListener listener) {
        this.listener = listener;
    }

    public Color getColor()               { return color; }
    public int getProducerId()            { return id; }
    public ProcessState getProcessState() { return processState; }
}