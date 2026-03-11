package processes;

import java.util.Random;

// Base class for Producer and Consumer — avoids code duplication (DRY)
// Both share: id, state, sleep times, listener pattern, and stop logic
public abstract class Process extends Thread {

    protected final int id;
    // This variable can be read or written by multiple threads, no cache
    protected volatile boolean running = true;
    protected volatile ProcessState processState = ProcessState.RUNNING;

    protected final int minSleep;
    protected final int maxSleep;
    protected final Random random = new Random();

    // States that represent the REAL state of the process — not just execution time
    // RUNNING  → actively trying to produce or consume
    // SLEEPING → resting voluntarily between operations
    // WAITING  → blocked — buffer full (producer) or empty (consumer)
    public enum ProcessState {
        RUNNING, SLEEPING, WAITING
    }

    // Generic listener — works for both Producer and Consumer (DRY)
    // Callback so UI knows process state
    public interface ProcessListener {

        void onStateChanged(int processId, ProcessState state);
    }

    private ProcessListener listener;

    public Process(int id, int minSleep, int maxSleep) {
        this.id = id;
        this.minSleep = minSleep;
        this.maxSleep = maxSleep;
    }

    // Each subclass defines its own type name — "Producer" or "Consumer"
    protected abstract String getProcessType();

    // Sets the state and notifies the listener (UI)
    protected void setProcessState(ProcessState state) {
        this.processState = state;
        if (listener != null) {
            listener.onStateChanged(id, state);
        }
    }

    public void stopProcess() {
        running = false;      // While ends naturally
        this.interrupt();     // If sleeping, wakes it with InterruptedException
    }

    public void setListener(ProcessListener listener) {
        this.listener = listener;
    }

    public int getProcessId() {
        return id;
    }

    public ProcessState getProcessState() {
        return processState;
    }
}
