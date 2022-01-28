package model.i;

public interface Provider<S, T> {
    T provide(S s);
}
