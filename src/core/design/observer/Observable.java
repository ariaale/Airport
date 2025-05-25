package core.design.observer;

import java.util.ArrayList;

public abstract class Observable {
    protected ArrayList<Observer> observers;

    public Observable() {
        this.observers = new ArrayList<>();
    }

    public void addObserver(Observer observer) {
        this.observers.add(observer);
        observer.setObservable(this);
    }

    protected void notifyAll(int value) {
        for (Observer observer : this.observers) {
            observer.notify(value);
        }
    }
}