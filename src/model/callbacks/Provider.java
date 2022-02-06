package model.callbacks;

public interface Provider<S, T> {
    T provide(S s);
}
