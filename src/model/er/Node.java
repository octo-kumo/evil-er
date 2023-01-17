package model.er;

import main.renderer.DiagramGraphics;
import model.Drawable;
import model.Vector;

import java.awt.geom.AffineTransform;

public abstract class Node extends Vector implements Drawable {
    public static boolean HAS_NODE_CHANGED = false;

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

    public void setX(double x) {
        this.x = x;
        HAS_NODE_CHANGED = true;
    }

    public void setY(double y) {
        this.y = y;
        HAS_NODE_CHANGED = true;
    }
}
