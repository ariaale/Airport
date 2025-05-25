package core.design.observer;

public abstract class Observer {
    protected Observable observable;

    public Observer() {
        this.observable = null;
    }

    public void setObservable(Observable observable) {
        this.observable = observable;
    }

    public abstract void notify(int value);
}