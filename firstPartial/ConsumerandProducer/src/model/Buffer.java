package model;

import java.awt.Color;

// Shared resource
public class Buffer {

    private final int capacity;
    private final Color[] slots;  // Each slot saves color by producer
    private int count;            // How many slots are busy
    private BufferListener listener;

    // Interface to notify UI
    public interface BufferListener {
        void onSlotFilled(int slotIndex, Color color);
        void onSlotCleared(int slotIndex);
    }

    // Interface WAITING callback
    @FunctionalInterface
    public interface OnWaitCallback {
        void onWaiting();
    }

    public Buffer(int capacity) {
        this.capacity = capacity;
        this.slots = new Color[capacity];
        this.count = 0;
    }

    public void setListener(BufferListener listener) {
        this.listener = listener;
    }

    // Producer calls this — it blocks if full (real WAITING)
    // Assures that even with 5 producers and 3 consumers,
    // just one gets into the buffer at a time.
    // All of this is critic section.
    // Communication: Mutual exclusion with synchronized — only one can get in.
    // Processes do not communicate each other, they do it through the buffer.
    // We use sleep & wakeup, and wait() so we don't lose the signal.
    public synchronized int produce(Color color, OnWaitCallback onWait)
            throws InterruptedException {
        while (count == capacity) {
            onWait.onWaiting();
            wait(); // Full buffer -> producer is blocked here
        }
        slots[count] = color; // Put its color on the next free slot
        int filledSlot = count;
        count++;
        System.out.println("Produced -> slot " + filledSlot + " color: " + color);
        if (listener != null) {
            listener.onSlotFilled(filledSlot, color);
        }
        notifyAll(); // Wakes up all that were waiting
        return filledSlot; // Returns which slot was filled
    }

    // Consumer calls this with FIFO (queue) — it blocks if empty (real WAITING)
    // Also critic section
    public synchronized int consume(OnWaitCallback onWait)
            throws InterruptedException {
        while (count == 0) {
            onWait.onWaiting();
            wait(); // Empty buffer -> consumer is blocked here
        }
        // FIFO: always consume slot 0 then shift everything to the left
        System.out.println("Consumed -> slot 0, color was: " + slots[0]);
        for (int i = 0; i < count - 1; i++) {
            slots[i] = slots[i + 1];
        }
        slots[count - 1] = null; // Clean last slot
        count--;
        // Notify UI to redraw all slots after shift
        if (listener != null) {
            for (int i = 0; i < capacity; i++) {
                if (slots[i] != null) {
                    listener.onSlotFilled(i, slots[i]);
                } else {
                    listener.onSlotCleared(i);
                }
            }
        }
        notifyAll();
        return 0; // Always 0 on FIFO but needed for UI
    }

    // For UI to read current state of buffer
    public synchronized Color[] getSlotsSnapshot() {
        return slots.clone();
    }

    public synchronized int getCount() {
        return count;
    }

    public int getCapacity() {
        return capacity;
    }
}