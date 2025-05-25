package core.storage;

import core.model.Location;
import core.design.observer.Observable;
import java.util.ArrayList;

public class LocationStorage extends Observable implements Storage<Location> {

    private static LocationStorage instance;
    private ArrayList<Location> locations;

    private LocationStorage() {
        this.locations = new ArrayList<>();
    }

    public static LocationStorage getInstance() {
        if (instance == null) {
            instance = new LocationStorage();
        }
        return instance;
    }

    @Override
    public boolean add(Location item) {
        boolean exists = locations.stream().anyMatch(l -> l.getAirportId().equals(item.getAirportId()));
        if (exists) {
            return false;
        }
        this.locations.add(item);
        notifyAll(1);
        return true;
    }

    @Override
    public Location get(String id) {
        return locations.stream()
                        .filter(location -> location.getAirportId().equals(id))
                        .findFirst()
                        .orElse(null);
    }

    public ArrayList<Location> getAll() {
        return new ArrayList<>(this.locations);
    }
}