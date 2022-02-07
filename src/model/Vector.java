package model;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.List;

public class Vector extends Point2D {
    @Expose
    protected double x;
    @Expose
    protected double y;
    public static final ImmutableVector ZERO = new ImmutableVector();

    public static Vector average(List<? extends Vector> nodes) {
        return nodes.parallelStream()
                .map(Vector.class::cast)
                .reduce(Vector::add)
                .orElse(Vector.ZERO).div(nodes.size());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public void setLocation(double x, double y) {
        set(x, y);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Vector(double x, double y) {
        set(x, y);
    }

    public Vector() {
        this(0, 0);
    }

    public Vector(Vector vector) {
        this(vector.x, vector.y);
    }

    public Vector set(@Nullable Vector other) {
        if (other == null) return this;
        return set(other.getX(), other.getY());
    }

    public Vector set(double x, double y) {
        setX(x);
        setY(y);
        return this;
    }

    public Vector add(Vector other) {
        return add(other.getX(), other.getY());
    }

    public Vector add(double x, double y) {
        return clone().incre(x, y);
    }

    public Vector minus(Vector other) {
        return minus(other.getX(), other.getY());
    }

    public Vector minus(double x, double y) {
        return clone().decre(x, y);
    }

    public Vector incre(Vector other) {
        return incre(other.getX(), other.getY());
    }

    public Vector incre(double x, double y) {
        setX(getX() + x);
        setY(getY() + y);
        return this;
    }

    public Vector decre(Vector other) {
        return decre(other.getX(), other.getY());
    }

    public Vector decre(double x, double y) {
        return incre(-x, -y);
    }

    public Vector scale(double scale) {
        return scale(scale, scale);
    }

    public Vector scale(double x, double y) {
        setX(this.getX() * x);
        setY(this.getY() * y);
        return this;
    }

    public Vector multi(double scale) {
        return clone().scale(scale);
    }

    public Vector multi(double x, double y) {
        return clone().scale(x, y);
    }

    public Vector divide(double scale) {
        return clone().div(scale);
    }

    public Vector div(double scale) {
        return scale(1 / scale);
    }

    public Vector norm() {
        double len = len();
        if (len == 0) return Vector.ZERO;
        return scale(1 / len);
    }

    public Vector log() {
        return this.normalized().scale(Math.log(len()));
    }

    public Vector normalized() {
        return clone().norm();
    }

    public double len() {
        return Math.hypot(getX(), getY());
    }

    public double len2() {
        return getY() * getY() + getX() * getX();
    }

    public double angle() {
        return Math.atan2(getY(), getX());
    }

    public Vector cap(double len) {
        double clen = len();
        if (clen == 0) return Vector.ZERO;
        if (clen > len) return scale(len / clen);
        return this;
    }

    public double dot(Vector other) {
        return getX() * other.getX() + getY() * other.getY();
    }

    public Vector round() {
        setX(Math.round(getX()));
        setY(Math.round(getY()));
        return this;
    }

    public Vector rounded() {
        return clone().round();
    }

    public Vector neg() {
        setX(-getX());
        setY(-getY());
        return this;
    }

    public Vector negate() {
        return clone().neg();
    }

    @Override
    public Vector clone() {
        return new Vector(getX(), getY());
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", getX(), getY());
    }

    public Vector rot90() {
        return new Vector(getY(), -getX());
    }
}
