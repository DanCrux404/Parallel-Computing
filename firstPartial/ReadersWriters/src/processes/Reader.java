package processes;

// Reader thread — reads flight data from the database
import models.DataBase;

// Multiple readers can read simultaneously — no exclusion between them
// Blocked only when a writer is active
// Inherits from Process: id, state, sleep times, listener, stop logic
public class Reader extends Process {

    private final DataBase database;

    private static final int MIN_SLEEP = 500;
    private static final int MAX_SLEEP = 1500;

    public Reader(int id, DataBase database) {
        super(id, MIN_SLEEP, MAX_SLEEP);
        this.database = database;
        setName("Reader-" + id); // Thread name — useful for debug
    }

    // The heart of the thread — executes when .start() is called
    @Override
    public void run() {
        while (running) {
            try {
                setProcessState(ProcessState.RUNNING);

                // Critic section start
                // If writer is active, lambda sets state to WAITING
                // and thread blocks inside database.startReading()

                database.startReading(() -> setProcessState(ProcessState.WAITING));
                // Multiple readers can be here simultaneously
                try {
                    database.readData(id);// Not synchronized — readers share access
                    //so we need a try cacth if is interrupted
                    //diferent from consume and produce cause thet are synchronized
                } finally {
                    database.stopReading(); // Exit DB — decrement readerCount
                }
                // Critic section end

                setProcessState(ProcessState.SLEEPING);
                int sleepTime = minSleep + random.nextInt(maxSleep - minSleep);
                Thread.sleep(sleepTime); // Goes to sleep by its own choice

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Reader-" + id + " stopped.");
    }

    @Override
    protected String getProcessType() {
        return "Reader";
    }
}
