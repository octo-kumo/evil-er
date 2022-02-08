package model.others;

public class Pair<A, B> {
    public A a;
    public B b;

    public Pair() {
        a = null;
        b = null;
    }

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }
}
