package model;

import main.renderer.DiagramGraphics;

import java.awt.geom.AffineTransform;

public abstract class Node implements Drawable {
    public double x, y;

    public <T extends Node> T setPos(double x, double y) {
        this.x = x;
        this.y = y;
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
