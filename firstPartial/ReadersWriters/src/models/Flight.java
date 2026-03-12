package models;

// Represents a flight record in the database
// Writers create or modify these — Readers read them
public class Flight {

    private final int id;
    private String origin;
    private String destination;
    private double price;
    private String status;

    // Possible statuses a writer can set
    public static final String[] STATUSES = {"Active", "Delayed", "Cancelled"};

    // Possible airports to use randomly
    public static final String[] AIRPORTS = {"GDL", "CDMX", "MTY", "CUN", "TIJ", "LAX", "JFK", "MIA"};

    public Flight(int id, String origin, String destination, double price, String status) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.price = price;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDest() {
        return destination;
    }

    public double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    // Setters — only Writers call these
    public void setPrice(double price) {
        this.price = price;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // For easy console debugging
    @Override
    public String toString() {
        return "Flight[" + id + " " + origin + "->" + destination + " $" + price + " " + status + "]";
    }
}
