package main.renderer;

public interface Provider<S, T> {
    T provide(S s);
}
