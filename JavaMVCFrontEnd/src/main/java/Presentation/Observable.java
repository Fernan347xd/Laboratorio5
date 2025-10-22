package Presentation;

import Utilities.EventType;
import java.util.ArrayList;
import java.util.List;

public class Observable {

    private final List<IObserver> observers = new ArrayList<>();

    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    protected void notifyObservers(EventType eventType, Object data) {
        for (IObserver observer : observers) {
            observer.update(eventType, data);
        }
    }
}