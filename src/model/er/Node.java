package model.er;

import main.renderer.DiagramGraphics;
import model.Drawable;
import model.Vector;

import java.awt.geom.AffineTransform;

public abstract class Node extends Vector implements Drawable {
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
