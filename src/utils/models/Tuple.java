package utils.models;

import com.google.gson.annotations.Expose;

public class Tuple<A, B, C> {
    @Expose
    public A a;
    @Expose
    public B b;
    @Expose
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
