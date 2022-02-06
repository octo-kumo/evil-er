package model.others;

import model.callbacks.ChangeListener;

import java.util.ArrayList;

public class Reactive<T> {
    private final ArrayList<ChangeListener<T>> listeners;
    private T value;

    public Reactive() {
        this(null);
    }

    public Reactive(T value) {
        this.value = value;
        listeners = new ArrayList<>();
    }

    public T get() {
        return value;
    }

    public void set(T nv) {
        this.value = nv;
        listeners.forEach(l -> l.onChange(nv));
    }

    public void addListener(ChangeListener<T> listener) {
        listeners.add(listener);
        listener.onChange(value);
    }

    public void removeListener(ChangeListener<T> listener) {
        listeners.remove(listener);
    }
}
