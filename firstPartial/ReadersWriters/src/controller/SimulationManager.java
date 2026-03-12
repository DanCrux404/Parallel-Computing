package controller;

// Brain of the simulation — intermediary between threads and UI
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import models.DataBase;
import models.Flight;
import processes.Reader;
import processes.Writer;
import readerswriters.windows.MainWindow;
import readerswriters.windows.MonitorWindow;
import processes.Process;
import util.ColorManager;

// Neither threads touch Swing, nor UI knows about threads
public class SimulationManager implements DataBase.DatabaseListener {

    private final DataBase database;
    private final ColorManager colorManager;
    private final MainWindow window;
    private final MonitorWindow monitorWindow;

    // Active threads lists — LIFO for delete (we remove from the end)
    private final List<Reader> readers = new ArrayList<>();
    private final List<Writer> writers = new ArrayList<>();

    // ID counters — they only go up, even if we delete
    // so Reader-3 is always Reader-3, never reused
    private int nextReaderId = 1;
    private int nextWriterId = 1;

    public SimulationManager(MainWindow window) {
        this.window = window;
        this.database = new DataBase();
        this.colorManager = new ColorManager();

        // Database notifies us when data changes — we update the UI
        database.setListener(this);

        // Open monitor window alongside main window
        this.monitorWindow = new MonitorWindow();
        this.monitorWindow.setVisible(true);
    }

    // ======= READERS =============================================
    public void addReader() {
        int id = nextReaderId++;

        Reader reader = new Reader(id, database);

        // Readers have no color — null shows gray in monitor
        monitorWindow.addProcess("R-" + id, "Reader", null);

        // One listener type for all processes — DRY
        reader.setListener((processId, state) -> {
            monitorWindow.updateState("R-" + processId, state.name());
            // Just finished reading — increment operations counter
            if (state == Process.ProcessState.SLEEPING) {
                monitorWindow.incrementCount("R-" + processId);
            }
        });

        readers.add(reader);
        reader.start(); // Here the thread is born — run() starts executing

        updateReaderCount();
    }

    public void removeReader() {
        if (readers.isEmpty()) {
            return;
        }

        // LIFO — remove the last one added
        Reader last = readers.remove(readers.size() - 1);
        last.stopProcess(); // Sets running=false and interrupts if sleeping
        monitorWindow.removeProcess("R-" + last.getProcessId());
        updateReaderCount();
    }

    // ======= WRITERS =============================================
    public void addWriter() {
        int id = nextWriterId++;
        Color color = colorManager.getNextColor(); // Each writer gets its own color

        Writer writer = new Writer(id, database, color);

        // Register in monitor with writer's color
        monitorWindow.addProcess("W-" + id, "Writer", color);

        // Same listener pattern as Reader — DRY
        writer.setListener((processId, state) -> {
            monitorWindow.updateState("W-" + processId, state.name());
            // Just finished writing — increment operations counter
            if (state == Process.ProcessState.SLEEPING) {
                monitorWindow.incrementCount("W-" + processId);
            }
        });

        writers.add(writer);
        writer.start(); // Thread born

        updateWriterCount();
    }

    public void removeWriter() {
        if (writers.isEmpty()) {
            return;
        }

        // LIFO — remove the last one added
        Writer last = writers.remove(writers.size() - 1);
        last.stopProcess();
        monitorWindow.removeProcess("W-" + last.getProcessId());
        updateWriterCount();
    }

    // ======= DATABASE LISTENER — called by Database, updates UI ==
    @Override
    public void onFlightAdded(Flight flight) {
        // Database calls us from a background thread — must use invokeLater
        // Only the EDT (Event Dispatch Thread) can touch Swing components
        javax.swing.SwingUtilities.invokeLater(()
                -> window.addFlightRow(flight)
        );
    }

    @Override
    public void onFlightModified(int index, Flight flight) {
        javax.swing.SwingUtilities.invokeLater(()
                -> window.updateFlightRow(index, flight)
        );
    }

    @Override
    public void onReadingHighlight(int index, boolean highlighting) {
        // Highlight or unhighlight the row being read
        javax.swing.SwingUtilities.invokeLater(()
                -> window.highlightRow(index, highlighting)
        );
    }

    @Override
    public void onRecordCountChanged(int count) {
        javax.swing.SwingUtilities.invokeLater(()
                -> window.setRecordCount(count)
        );
    }

    // ======= UI COUNTER UPDATES — always on EDT ==================
    private void updateReaderCount() {
        javax.swing.SwingUtilities.invokeLater(()
                -> window.setReaderCount(readers.size())
        );
    }

    private void updateWriterCount() {
        javax.swing.SwingUtilities.invokeLater(()
                -> window.setWriterCount(writers.size())
        );
    }
}
