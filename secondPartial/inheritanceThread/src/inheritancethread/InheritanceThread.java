package inheritancethread;

public class InheritanceThread {

    public static void main(String[] args) {
        new ThreadExample("Pepe").start();
        new ThreadExample("Juan").start();
        System.out.println("Termina thread main");
    }
    
}