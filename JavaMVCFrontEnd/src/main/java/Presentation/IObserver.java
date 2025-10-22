package Presentation;

import Utilities.EventType;

public interface IObserver {
    void update(EventType eventType, Object data);
}
