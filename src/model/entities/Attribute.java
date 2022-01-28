package model.entities;

import model.Vector;
import main.renderer.DiagramGraphics;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class Attribute extends Entity {
    public Entity parent;

    public boolean key;
    public boolean derived;

    public double getX() {
        return x + (parent == null ? 0 : parent.getX());
    }

    public double getY() {
        return y + (parent == null ? 0 : parent.getY());
    }

    public void pos(Vector n) {
        this.x = n.x - (parent == null ? 0 : parent.getX());
        this.y = n.y - (parent == null ? 0 : parent.getY());
    }

    public Attribute setParent(Entity parent) {
        this.parent = parent;
        return this;
    }

    public void drawShape(DiagramGraphics g, Shape shape) {
        Color toUse = g.getColor();
        if (isDerived()) g.dashed(shape, highlighted ? HIGHLIGHTED : Color.WHITE, toUse);
        else g.draw(shape, highlighted ? HIGHLIGHTED : Color.WHITE, toUse);
    }

    @Override
    public void prepaint(DiagramGraphics g) {
        super.prepaint(g);
        drawShape(g, new Line2D.Double(0, 0, -x, -y));
    }

    @Override
    public void paint(DiagramGraphics g) {
        super.paint(g);
        if (isKey()) drawShape(g, g.lineUnderString(name, 0, 3));
    }

    @Override
    public Shape getShape(double width, double height) {
        return new Ellipse2D.Double(-width / 2d, -height / 2d, width, height);
    }

    public boolean isKey() {
        return key;
    }

    public Attribute setKey(boolean key) {
        this.key = key;
        return this;
    }

    public boolean isDerived() {
        return derived;
    }

    public Attribute setDerived(boolean derived) {
        this.derived = derived;
        return this;
    }
}
