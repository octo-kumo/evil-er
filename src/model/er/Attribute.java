package model.er;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import model.Vector;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class Attribute extends Entity {
    private Entity parent;
    @Expose
    private boolean key;
    @Expose
    private boolean derived;

    public double getX() {
        return x + (getParent() == null ? 0 : getParent().getX());
    }

    public double getY() {
        return y + (getParent() == null ? 0 : getParent().getY());
    }

    public void setX(double x) {
        this.x = x - (getParent() == null ? 0 : getParent().getX());
    }

    public void setY(double y) {
        this.y = y - (getParent() == null ? 0 : getParent().getY());
    }

    public Vector truePosition() {
        return new Vector(x, y);
    }

    public void drawShape(DiagramGraphics g, Shape shape) {
        Color toUse = g.getColor();
        if (isDerived()) g.dashed(shape, isHighlighted() ? HIGHLIGHTED : Color.WHITE, toUse);
        else g.draw(shape, isHighlighted() ? HIGHLIGHTED : Color.WHITE, toUse);
    }

    @Override
    public void prepaint(DiagramGraphics g) {
        super.prepaint(g);
        g.draw(new Line2D.Double(Vector.ZERO, truePosition().neg()));
    }

    @Override
    public void paint(DiagramGraphics g) {
        super.paint(g);
        if (isKey()) {
            if (parent.isWeak()) g.dashed(g.lineUnderString(getName(), 0, 3));
            else drawShape(g, g.lineUnderString(getName(), 0, 3));
        }
    }

    @Override
    public Shape getShape(double width, double height) {
        // Ensure at least 7 px per character
        double newWidth = Math.max(width * 0.7, getName().length() * 7);
        return new Ellipse2D.Double(-newWidth / 2d, -height * 0.7 / 2d, newWidth, height * 0.7);
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

    public Entity getParent() {
        return parent;
    }

    public Attribute setParent(Entity parent) {
        this.parent = parent;
        return this;
    }
}
