package processes;

import java.awt.Color;
import model.Buffer;

// Producer thread — fills buffer slots with its own color
// Inherits from Process: id, state, sleep times, listener, stop logic
public class Producer extends Process {

    private final Buffer buffer;
    private final Color color; // Each producer has its own unique color

    // Producer is faster than Consumer
    private static final int MIN_SLEEP = 500;  // Minimum ms between productions
    private static final int MAX_SLEEP = 1500; // Maximum ms between productions

    public Producer(int id, Buffer buffer, Color color) {
        super(id, MIN_SLEEP, MAX_SLEEP);
        this.buffer = buffer;
        this.color = color;
        setName("Producer-" + id); // Thread name — useful for debug
    }

    @Override
    protected String getProcessType() {
        return "Producer"; // Used for thread name — "Producer-1"
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

    public Color getColor() {
        return color;
    }
}
