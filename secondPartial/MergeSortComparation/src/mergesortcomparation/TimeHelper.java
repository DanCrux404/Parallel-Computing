package mergesortcomparation;

public class TimeHelper {
    private static long secuentialTime = 0;

    public static long startTimer() {
        return System.nanoTime();
    }

    public static long showTime(long startTime, String label) {
        long endTime = System.nanoTime();
        long durationInMs = (endTime - startTime) / 1_000_000;
        System.out.println("Execution time (" + label + "): " + durationInMs + " ms");
        return durationInMs;
    }

    public static void saveSecuentialTime(long time) {
        secuentialTime = time;
    }

    public static void showEfficiency(long parallelTime) {
        if (secuentialTime == 0 || parallelTime == 0) {
            System.out.println("Time cannot be calculated (time = 0, array to short)");
            return;
        }

        int cores = Runtime.getRuntime().availableProcessors();
        double speedup = (double) secuentialTime / parallelTime;
        double efficiency = (speedup / cores) * 100;

        System.out.println("\n--- Comparation ---");
        System.out.println("Available cores : " + cores);
        System.out.printf("Speedup             : %.2fx%n", speedup);
        System.out.printf("Efficiency          : %.2f%%%n", efficiency);

        if (speedup > 1)
            System.out.println("Concurent was: " + String.format("%.2f", speedup) + "x faster");
        else
            System.out.println("Secuential was faster D: (Short array or few overhead)");
    }
}