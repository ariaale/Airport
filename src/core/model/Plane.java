package core.model;

import core.design.prototype.Prototype;
import java.util.ArrayList;

public class Plane implements Prototype<Plane>{
    
    private final String id;
    private String brand;
    private String model;
    private final int maxCapacity;
    private String airline;
    private ArrayList<Flight> flights;

    public Plane(String id, String brand, String model, int maxCapacity, String airline) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.maxCapacity = maxCapacity;
        this.airline = airline;
        this.flights = new ArrayList<>();
    }

    public void addFlight(Flight flight) {
        this.flights.add(flight);
    }
    
    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public String getAirline() {
        return airline;
    }

    public ArrayList<Flight> getFlights() {
        return flights;
    }
    
    public void setFlights(ArrayList<Flight> flights) {
        this.flights = flights;
    }
    
    public int getNumFlights() {
        return flights.size();
    }

    
    
    @Override
    public Plane clone(){
        
        Plane copy = new Plane(this.id,this.brand,this.model,this.maxCapacity,this.airline);
        copy.setFlights(this.flights);
        
        return copy;

    }
}
