package core.storage;

import core.model.Plane;
import core.design.observer.Observable;
import java.util.ArrayList;

public class PlaneStorage extends Observable implements Storage<Plane> {

    private static PlaneStorage instance;
    private ArrayList<Plane> planes;

    private PlaneStorage() {
        this.planes = new ArrayList<>();
    }

    public static PlaneStorage getInstance() {
        if (instance == null) {
            instance = new PlaneStorage();
        }
        return instance;
    }

    @Override
    public boolean add(Plane item) {
        boolean exists = planes.stream().anyMatch(p -> p.getId().equals(item.getId()));
        if (exists) {
            return false;
        }
        this.planes.add(item);
        notifyAll(1);
        return true;
    }

    @Override
    public Plane get(String id) {
        return planes.stream()
                     .filter(plane -> plane.getId().equals(id))
                     .findFirst()
                     .orElse(null);
    }

    public ArrayList<Plane> getAll() {
        return new ArrayList<>(this.planes);
    }
}