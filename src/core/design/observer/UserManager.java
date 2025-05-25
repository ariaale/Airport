package core.design.observer;

import core.model.Passenger;

public class UserManager extends Observable {

    private static UserManager instance;
    private Passenger currentUser;

    public static final int USER_CHANGED = 3;

    private UserManager() {
        this.currentUser = null;
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void setCurrentUser(Passenger user) {
        this.currentUser = user;
        notifyAll(USER_CHANGED);
    }

    public Passenger getCurrentUser() {
        return currentUser;
    }
}