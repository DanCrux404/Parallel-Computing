package processes;

import model.Buffer;

// Consumer thread — empties buffer slots (FIFO)
// Inherits from Process: id, state, sleep times, listener, stop logic
public class Consumer extends Process {

    private final Buffer buffer;

    // Consumer is slower than Producer — creates interesting WAITING states
    private static final int MIN_SLEEP = 1000; // Minimum ms between consumptions
    private static final int MAX_SLEEP = 2500; // Maximum ms between consumptions

    public Consumer(int id, Buffer buffer) {
        super(id, MIN_SLEEP, MAX_SLEEP);
        this.buffer = buffer;
        setName("Consumer-" + id); // Thread name — useful for debug
    }

    @Override
    protected String getProcessType() {
        return "Consumer"; // Used for thread name — "Consumer-1"
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
}
