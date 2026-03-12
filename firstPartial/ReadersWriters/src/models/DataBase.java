package models;

// Shared resource — models a flight reservation database

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Multiple readers can read simultaneously
// Only one writer at a time — and no readers while writing
// This is the readers-writers lock pattern
public class DataBase {

    // The actual data — list of flight records
    private final List<Flight> flights = new ArrayList<>();

    // Readers-writers lock state
    private int readerCount = 0;    // How many readers are reading RIGHT NOW
    private boolean writing = false; // Is a writer currently writing?

    private int nextFlightId = 1;
    private final Random random = new Random();

    private DatabaseListener listener;

    // Interface to notify UI of changes — Database never touches Swing directly
    public interface DatabaseListener {

        void onFlightAdded(Flight flight);

        void onFlightModified(int index, Flight flight);

        void onReadingHighlight(int index, boolean highlighting); // highlight row while reading

        void onRecordCountChanged(int count);
    }

    public void setListener(DatabaseListener listener) {
        this.listener = listener;
    }

    // == READER enters blocks if a writer is active ==================
    // Multiple readers CAN be here simultaneously
    // Critic section readerCount is shared between all reader threads
    public synchronized void startReading(ReadWriteCallback onWaiting)
            throws InterruptedException {
        while (writing) {
            onWaiting.onWaiting(); // Reader waits — writer is active
            wait();
        }
        readerCount++; // One more reader is now reading
        System.out.println("Reader entered — active readers: " + readerCount);
    }

    // == READER exits if last reader, wake up waiting writers =======
    public synchronized void stopReading() {
        readerCount--;
        System.out.println("Reader exited active readers: " + readerCount);
        if (readerCount == 0) {
            notifyAll(); // Last reader left writers can now enter
        }
    }

    // == WRITER enters blocks if anyone is reading or writing ===========
    // Only ONE writer can be here and no readers allowed
    // Critic section writing and flights list are shared
    // If it wasn't synchronized two threads might enter 
    // at the same time and could break mutual exclution
    public synchronized void startWriting(ReadWriteCallback onWaiting)
            throws InterruptedException {
        while (writing || readerCount > 0) {
            onWaiting.onWaiting(); // Writer waits — readers or writer active
            wait();
        }
        writing = true; // Lock acquired — writer is now active
        System.out.println("Writer entered — writing started");
    }

    // == WRITER exits — wake up everyone waiting =======================
    public synchronized void stopWriting() {
        writing = false; // Lock released
        System.out.println("Writer exited — writing finished");
        notifyAll(); // Wake up all waiting readers and writers
    }

    // == READ operation — called between startReading/stopReading ======
    // Not synchronized — multiple readers can read simultaneously
    public void readData(int readerId) throws InterruptedException {
        if (flights.isEmpty()) {
            return;
        }

        // Pick a random flight to "read"
        int index = random.nextInt(flights.size());
        Flight flight = flights.get(index);

        // Highlight the row being read in UI
        if (listener != null) {
            listener.onReadingHighlight(index, true);
        }

        System.out.println("Reader-" + readerId + " reading: " + flight);
        //Reading takes time, but other readers can also read 
        Thread.sleep(500); // Simulate reading time

        // Remove highlight
        if (listener != null) {
            listener.onReadingHighlight(index, false);
        }
    }

    // == WRITE operation — called between startWriting/stopWriting ==========
    public void writeData() {
        if (flights.isEmpty() || random.nextBoolean()) {
            // Add a new flight
            String origin = Flight.AIRPORTS[random.nextInt(Flight.AIRPORTS.length)];
            String dest = Flight.AIRPORTS[random.nextInt(Flight.AIRPORTS.length)];
            double price = 500 + random.nextInt(1500);
            String status = Flight.STATUSES[random.nextInt(Flight.STATUSES.length)];

            Flight newFlight = new Flight(nextFlightId++, origin, dest, price, status);
            flights.add(newFlight);

            System.out.println("Writer added: " + newFlight);
            if (listener != null) {
                listener.onFlightAdded(newFlight);
                listener.onRecordCountChanged(flights.size());
            }
        } else {
            // Modify an existing flight
            int index = random.nextInt(flights.size());
            Flight flight = flights.get(index);
            flight.setPrice(500 + random.nextInt(1500));
            flight.setStatus(Flight.STATUSES[random.nextInt(Flight.STATUSES.length)]);
            
            System.out.println("Writer modified: " + flight);
            if (listener != null) {
                listener.onFlightModified(index, flight);
            }
        }
    }

    // === Getters ===================================================
    public synchronized int getReaderCount() {
        return readerCount;
    }

    public synchronized boolean isWriting() {
        return writing;
    }

    public synchronized int getFlightCount() {
        return flights.size();
    }

    // Functional interface for waiting callback — same pattern as Buffer
    @FunctionalInterface
    public interface ReadWriteCallback {

        void onWaiting();
    }
}
