package processes;

import java.util.Random;

public abstract class Process extends Thread {

    protected final int id;
    // This variable can be read or written by multiple threads, no cache
    protected volatile boolean running = true;
    protected volatile ProcessState processState = ProcessState.RUNNING;

    protected final int minSleep;
    protected final int maxSleep;
    protected final Random random = new Random();

    // States that represent the REAL state of the process — not just execution time
    // RUNNING  → actively trying to read or write
    // SLEEPING → resting voluntarily between operations
    // WAITING  → blocked — DB waiting to be read or writen
    public enum ProcessState {
        RUNNING, SLEEPING, WAITING
    }

    // Generic listener — works for both reader and writer (DRY)
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

    // Each subclass defines its own type name — "Reader" or "Writer"
    // Used for identification and debugging purposes
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

}
