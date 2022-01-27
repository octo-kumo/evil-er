package model;

import java.awt.*;
import java.awt.geom.AffineTransform;

public abstract class Node implements Drawable {
    public int x, y;

    public <T extends Node> T setPos(int x, int y) {
        this.x = x;
        this.y = y;
        return (T) this;
    }

    public void predraw(Graphics2D g) {
        AffineTransform transform = g.getTransform();
        g.translate(x, y);
        prepaint(g);
        g.setTransform(transform);
    }

    public void draw(Graphics2D g) {
        AffineTransform transform = g.getTransform();
        g.translate(x, y);
        paint(g);
        g.setTransform(transform);
    }

    public abstract void paint(Graphics2D g);

    public void prepaint(Graphics2D g) {
    }

    public static void draw(Graphics2D g, Shape shape) {
        g.setColor(Color.WHITE);
        g.fill(shape);
        g.setColor(Color.BLACK);
        g.draw(shape);
    }
}
