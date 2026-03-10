package controller;

// Brain of the simulation — intermediary between threads and UI
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import model.Buffer;
import processes.Consumer;
import processes.Producer;
import util.ColorManager;
import windows.MainWindow;
import windows.MonitorWindow;

// Neither threads touch Swing, nor UI knows about threads
public class SimulationManager implements Buffer.BufferListener {

    private final Buffer buffer;
    private final ColorManager colorManager;
    private final MainWindow window;

    // Active threads lists — LIFO for delete (we remove from the end)
    private final List<Producer> producers = new ArrayList<>();
    private final List<Consumer> consumers = new ArrayList<>();

    // ID counters — they only go up, even if we delete
    // so Producer-3 is always Producer-3, never reused
    private int nextProducerId = 1;
    private int nextConsumerId = 1;

    private final MonitorWindow monitorWindow;

    public SimulationManager(MainWindow window) {
        this.window = window;
        this.buffer = new Buffer(10);
        this.colorManager = new ColorManager();

        // Buffer notifies us when a slot changes — we update the UI
        buffer.setListener(this);
        this.monitorWindow = new MonitorWindow();
        this.monitorWindow.setVisible(true);
    }

    //======PRODUCERS =========================================
    public void addProducer() {
        Color color = colorManager.getNextColor(); // Each producer gets its own color
        int id = nextProducerId++;

        Producer producer = new Producer(id, buffer, color);

        // When producer state changes, update UI label color
        //And send info to monitorWindow
        monitorWindow.addProcess("P-" + id, "Producer", color);
        producer.setListener((producerId, state) -> {
            monitorWindow.updateState("P-" + producerId, state.name());
            if (state == Producer.ProcessState.SLEEPING) {
                monitorWindow.incrementCount("P-" + producerId);
            }
        });

        producers.add(producer);
        producer.start(); // Here the thread is born — run() starts executing

        // Update north counter on EDT
        updateProducerCount();
    }

    public void removeProducer() {
        if (producers.isEmpty()) {
            return;
        }

        // LIFO — remove the last one added
        Producer last = producers.remove(producers.size() - 1);
        last.stopProducer(); // Sets running=false and interrupts if sleeping
        monitorWindow.removeProcess("P-" + last.getProducerId());
        updateProducerCount();
    }

    // ======CONSUMERS =========================================
    public void addConsumer() {
        int id = nextConsumerId++;

        Consumer consumer = new Consumer(id, buffer);

        monitorWindow.addProcess("C-" + id, "Consumer", null);
        consumer.setListener((consumerId, state) -> {
            monitorWindow.updateState("C-" + consumerId, state.name());
            if (state == Consumer.ProcessState.SLEEPING) {
                monitorWindow.incrementCount("C-" + consumerId);
            }
        });

        consumers.add(consumer);
        consumer.start(); // Thread born

        updateConsumerCount();
    }

    public void removeConsumer() {
        if (consumers.isEmpty()) {
            return;
        }

        // LIFO — remove the last one added
        Consumer last = consumers.remove(consumers.size() - 1);
        last.stopConsumer();
        monitorWindow.removeProcess("C-" + last.getConsumerId());
        updateConsumerCount();
    }

    // =====BUFFER LISTENER — called by Buffer, updates UI ======================
    @Override
    public void onSlotFilled(int slotIndex, Color color) {
        // Buffer calls us from a background thread — must use invokeLater
        // Only the EDT (Event Dispatch Thread) can touch Swing components
        javax.swing.SwingUtilities.invokeLater(() -> {
            window.setCellColor(slotIndex, color);
            updateBufferCount();
        });
    }

    @Override
    public void onSlotCleared(int slotIndex) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            window.setCellColor(slotIndex, Color.WHITE);
            updateBufferCount();
        });
    }

    // ========= UI COUNTER UPDATES — always on EDT ===================
    private void updateProducerCount() {
        javax.swing.SwingUtilities.invokeLater(()
                -> window.setProducerCount(producers.size())
        );
    }

    private void updateConsumerCount() {
        javax.swing.SwingUtilities.invokeLater(()
                -> window.setConsumerCount(consumers.size())
        );
    }

    private void updateBufferCount() {
        // buffer.getCount() is synchronized — safe to call from any thread
        window.setBufferCount(buffer.getCount());
    }
}
