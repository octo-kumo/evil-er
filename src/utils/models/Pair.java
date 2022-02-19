package utils.models;

import com.google.gson.annotations.Expose;

public class Pair<A, B> {
    @Expose
    public A a;
    @Expose
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
