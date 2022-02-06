package model;

public class ImmutableVector extends Vector {
    public ImmutableVector(double x, double y) {
        super(x, y);
    }

    public ImmutableVector() {
        super(0, 0);
    }

    public void setX(double x) {
    }

    public void setY(double y) {
    }
}