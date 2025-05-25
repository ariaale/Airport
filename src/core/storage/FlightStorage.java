package core.storage;

import core.model.Flight;
import core.design.observer.Observable;
import java.util.ArrayList;

public class FlightStorage extends Observable implements Storage<Flight> {

    private static FlightStorage instance;
    private ArrayList<Flight> flights;

    private FlightStorage() {
        this.flights = new ArrayList<>();
    }

    public static FlightStorage getInstance() {
        if (instance == null) {
            instance = new FlightStorage();
        }
        return instance;
    }

    @Override
    public boolean add(Flight item) {
        boolean exists = flights.stream().anyMatch(f -> f.getId().equals(item.getId()));
        if (exists) {
            return false;
        }
        this.flights.add(item);
        notifyAll(1);
        return true;
    }
    
    public boolean update(Flight item) {
        for (int i = 0; i < this.flights.size(); i++) {
            if (this.flights.get(i).getId().equals(item.getId())) {
                this.flights.set(i, item);
                notifyAll(2);
                return true;
            }
        }
        return false;
    }

    @Override
    public Flight get(String id) {  
        return flights.stream()
                      .filter(flight -> flight.getId().equals(id))
                      .findFirst()
                      .orElse(null);
    }

    public ArrayList<Flight> getAll() {
        return new ArrayList<>(this.flights); 
    }
}