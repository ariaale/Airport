package core.storage;

import core.model.Passenger;
import core.design.observer.Observable;
import java.util.ArrayList;

public class PassengerStorage extends Observable implements Storage<Passenger> {

    private static PassengerStorage instance;
    private ArrayList<Passenger> passengers;

    private PassengerStorage() {
        this.passengers = new ArrayList<>();
    }

    public static PassengerStorage getInstance() {
        if (instance == null) {
            instance = new PassengerStorage();
        }
        return instance;
    }

    @Override
    public boolean add(Passenger item) {
        boolean exists = passengers.stream().anyMatch(p -> p.getId() == item.getId());
        if (exists) {
            return false;
        }
        this.passengers.add(item);
        notifyAll(1);
        return true;
    }

    public boolean update(Passenger item) {
        for (int i = 0; i < this.passengers.size(); i++) {
            if (this.passengers.get(i).getId() == item.getId()) {
                this.passengers.set(i, item);
                notifyAll(2);
                return true;
            }
        }
        return false;
    }

    @Override
    public Passenger get(String id) {
        try {
            Long idLong = Long.parseLong(id);
            return passengers.stream()
                             .filter(passenger -> passenger.getId() == idLong)
                             .findFirst()
                             .orElse(null);
        } catch (NumberFormatException e) {
            return null; 
        }
    }

    public ArrayList<Passenger> getAll() {
        return new ArrayList<>(this.passengers);
    }
}