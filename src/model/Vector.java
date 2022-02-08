package model;

import com.google.gson.annotations.Expose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.List;

public class Vector extends Point2D {
    @Expose
    protected double x;
    @Expose
    protected double y;
    public static final ImmutableVector ZERO = new ImmutableVector();

    public static Vector average(@NotNull List<? extends Vector> nodes) {
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

    public Vector(@NotNull Point2D other) {
        this(other.getX(), other.getY());
    }

    public Vector set(@Nullable Point2D other) {
        if (other == null) return this;
        return set(other.getX(), other.getY());
    }

    public Vector set(double x, double y) {
        setX(x);
        setY(y);
        return this;
    }

    public Vector add(@NotNull Point2D other) {
        return add(other.getX(), other.getY());
    }

    public Vector add(double x, double y) {
        return clone().incre(x, y);
    }

    public Vector minus(@NotNull Point2D other) {
        return minus(other.getX(), other.getY());
    }

    public Vector minus(double x, double y) {
        return clone().decre(x, y);
    }

    public Vector incre(@NotNull Point2D other) {
        return incre(other.getX(), other.getY());
    }

    public Vector incre(double x, double y) {
        setX(getX() + x);
        setY(getY() + y);
        return this;
    }

    public Vector decre(@NotNull Point2D other) {
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

    public double dot(@NotNull Point2D other) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector vector = (Vector) o;
        return vector.minus(this).len() < 1;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = java.lang.Double.doubleToLongBits(getX());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = java.lang.Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f)", getX(), getY());
    }

    public Vector rot90() {
        double newX = this.getY();
        setY(-getX());
        setX(newX);
        return this;
    }

    public Vector rotate90() {
        return clone().rot90();
    }

    public static double alwaysUp(double radians) {
        double det = (radians / Math.PI) % 2;
        if (det < 0) det += 2;
        return det > 1 ? radians : radians + Math.PI;
    }
}
