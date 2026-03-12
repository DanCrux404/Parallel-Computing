package processes;

// Writer thread — adds or modifies flight records in the database
import java.awt.Color;
import models.DataBase;

// Only ONE writer at a time — and NO readers allowed while writing
// Blocked if any reader is active OR another writer is active
// Inherits from Process: id, state, sleep times, listener, stop logic
public class Writer extends Process {

    private final DataBase database;
    private final Color color; // Each writer has its own color for the monitor

    private static final int MIN_SLEEP = 1000; // Writer is slower than Reader
    private static final int MAX_SLEEP = 2500;

    public Writer(int id, DataBase database, Color color) {
        super(id, MIN_SLEEP, MAX_SLEEP);
        this.database = database;
        this.color = color;
        setName("Writer-" + id); // Thread name — useful for debug
    }

    // The heart of the thread — executes when .start() is called
    @Override
    public void run() {
        while (running) {
            try {
                setProcessState(ProcessState.RUNNING);

                // Critic section start
                // If DB is busy, lambda sets state to WAITING
                // and thread blocks inside database.startWriting()

                database.startWriting(() -> setProcessState(ProcessState.WAITING));
                // Exclusive access guaranteed — no readers, no other writers
                try {
                    database.writeData(); // Add or modify a flight
                } finally {
                    database.stopWriting(); // Release lock — notify everyone waiting
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
        System.out.println("Writer-" + id + " stopped.");
    }

    public Color getColor() {
        return color;
    }

    @Override
    protected String getProcessType() {
        return "Writer";
    }
}
