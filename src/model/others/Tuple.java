package model.others;

public class Tuple<A, B, C> {
    public A a;
    public B b;
    public C c;

    public Tuple() {
        this(null, null, null);
    }

    public Tuple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
