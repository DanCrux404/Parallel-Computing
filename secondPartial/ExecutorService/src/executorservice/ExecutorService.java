package executorservice;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author dante
 */
public class ExecutorService {

    /**
     * @param args the command line arguments
     */
    /*
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(new ThreadExample("Hugo"));
        executor.execute(new ThreadExample("Paco"));
        executor.execute(new ThreadExample("Luis"));
        executor.shutdown();
// Esto intentará detener todas las tareas en ejecución de inmediato, no se dan
// garantías para detener las tareas, es un intento de mejor esfuerzo
//executor.shutdownNow();
// Bloqueará el hilo que lo llamó hasta que se haya terminado por completo o hasta que
// se agote el tiempo de espera.
// Normalmente se llama después de shutdown() o shutdownNow().
//executor.awaitTermination(10_000, TimeUnit.MILLISECONDS);
    }
     */
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(() -> doLongWork("Hugo"));
        executor.execute(() -> doLongWork("Paco"));
        executor.execute(() -> doLongWork("Luis"));
        executor.shutdown();
    }

    private static void doLongWork(String msg) {
        for (int i = 1; i <= 10; i++) {
            System.out.println("Running " + msg + ": " + i);
        }
    }
}
