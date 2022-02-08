package model;

public class Range {
    private double x1;
    private double x2;

    public Range() {
        this(0, 0);
    }

    public Range(double x1, double x2) {
        this.x1 = x1;
        this.x2 = x2;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public boolean intersects(Range other) {
        return x1 < other.x2 && other.x1 < x2;
    }
}
