package interfacethread;

/**
 *
 * @author dante
 */
public class InterfaceThread {

    public static void main(String[] args) {
        
        Thread Pepethread = new Thread(new ThreadExample(), "Pepe");
        Thread Juanthread = new Thread(new ThreadExample(), "Juan");
        
        Pepethread.setPriority(Thread.MIN_PRIORITY); //1
        Juanthread.setPriority(Thread.MAX_PRIORITY); //10
        
        Pepethread.start();
        Juanthread.start();

        System.out.println("Termina thread main");
    }
    
}
