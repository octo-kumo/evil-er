package model;

import java.awt.geom.Point2D;

public class Vector extends Point2D.Double {
    public Vector(double x, double y) {
        super(x, y);
    }

    public Vector() {
        this(0, 0);
    }

    public Vector add(Vector other) {
        return add(other.x, other.y);
    }

    public Vector add(double x, double y) {
        return new Vector(this.x + x, this.y + y);
    }

    public Vector minus(Vector other) {
        return minus(other.x, other.y);
    }

    public Vector minus(double x, double y) {
        return new Vector(this.x - x, this.y - y);
    }

    public Vector times(double scale) {
        return new Vector(this.x * scale, this.y * scale);
    }

    public Vector incre(Vector other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector decre(Vector other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vector scale(double scale) {
        this.x *= scale;
        this.y *= scale;
        return this;
    }

    public Vector norm() {
        return scale(1 / len());
    }

    public Vector normalize() {
        return times(1 / len());
    }

    public double len() {
        return Math.hypot(x, y);
    }

    public double ang() {
        return Math.atan2(y, x);
    }

    public Vector cap(double len) {
        double clen = len();
        if (clen > len) return scale(len / clen);
        return this;
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", x, y);
    }

    public double dot(Vector other) {
        return x * other.x + y * other.y;
    }
}
