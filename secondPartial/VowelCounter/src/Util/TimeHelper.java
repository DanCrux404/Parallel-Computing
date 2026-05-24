package Util;

/**
 *
 * @author dante
 */
// Utility class for measuring execution time
// Used by both SequentialCounter and ConcurrentCounter
// Console is a shared resource — print minimally!
public class TimeHelper {

    // == Start a timer — returns current time in nanoseconds =======
    public static long startTimer() {
        return System.nanoTime();
    }

    // == Stop timer and return duration in milliseconds =============
    // label is only for console — keep calls minimal in concurrent mode
    public static long stopTimer(long startTime, String label) {
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        // Minimal print — console is a shared resource!
        System.out.println(label + ": " + durationMs + "ms");
        return durationMs;
    }

    // == Stop timer silently — no console print =====================
    // Used in concurrent mode to avoid console contention
    public static long stopTimerSilent(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    // == Calculate and return comparison stats ======================
    public static ComparisonResult compare(long sequentialMs, long concurrentMs) {
        if (sequentialMs == 0 || concurrentMs == 0) {
            return new ComparisonResult(0, 0, 0, false);
        }

        int cores = Runtime.getRuntime().availableProcessors();
        double speedup = (double) sequentialMs / concurrentMs;
        double efficiency = (speedup / cores) * 100;
        boolean concurrentWon = speedup > 1;

        // Only one print — not inside a loop
        System.out.println("Sequential: " + sequentialMs + "ms | "
                + "Concurrent: " + concurrentMs + "ms | "
                + String.format("Speedup: %.2fx", speedup));

        return new ComparisonResult(speedup, efficiency, cores, concurrentWon);
    }

    // == Simple data class for comparison results ===================
    // Avoids passing 4 separate values around
    public static class ComparisonResult {

        public final double speedup;
        public final double efficiency;
        public final int cores;
        public final boolean concurrentWon;

        public ComparisonResult(double speedup, double efficiency,
                int cores, boolean concurrentWon) {
            this.speedup = speedup;
            this.efficiency = efficiency;
            this.cores = cores;
            this.concurrentWon = concurrentWon;
        }

        // Formatted strings for UI labels
        public String getSpeedupText() {
            return String.format("%.2fx", speedup);
        }

        public String getEfficiencyText() {
            return String.format("%.2f%%", efficiency);
        }
    }
}
