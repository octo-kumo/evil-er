package model;

import main.renderer.DiagramGraphics;

import java.awt.geom.AffineTransform;

public abstract class Node implements Drawable {
    protected double x, y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double setX(double x) {
        return this.x = x;
    }

    public double setY(double y) {
        return this.y = y;
    }

    public <T extends Node> T setPos(double x, double y) {
        setX(x);
        setY(y);
        return (T) this;
    }

    public void predraw(DiagramGraphics g) {
        AffineTransform transform = g.getTransform();
        g.translate(x, y);
        prepaint(g);
        g.setTransform(transform);
    }

    public void draw(DiagramGraphics g) {
        AffineTransform transform = g.getTransform();
        g.translate(x, y);
        paint(g);
        g.setTransform(transform);
    }

    public abstract void paint(DiagramGraphics g);

    public void prepaint(DiagramGraphics g) {
    }
}
