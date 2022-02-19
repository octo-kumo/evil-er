package model.er;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import model.Vector;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import static main.er.ERDiagram.UNIVERSAL_METRICS;

public class Attribute extends Entity {

    public enum AttributeType {
        Char, String, Boolean, Integer, Float, DateTime, Date
    }

    private Entity parent;
    @Expose
    private boolean key;
    @Expose
    private boolean derived;
    @Expose
    private AttributeType dataType;

    public double getX() {
        return x + (getParent() == null ? 0 : getParent().getX());
    }

    public void setX(double x) {
        this.x = x - (getParent() == null ? 0 : getParent().getX());
    }

    public double getY() {
        return y + (getParent() == null ? 0 : getParent().getY());
    }

    public void setY(double y) {
        this.y = y - (getParent() == null ? 0 : getParent().getY());
    }

    public AttributeType getDataType() {
        return dataType;
    }

    public void setDataType(AttributeType dataType) {
        this.dataType = dataType;
    }

    public Vector truePosition() {
        return new Vector(x, y);
    }

    public void drawShape(DiagramGraphics g, Shape shape) {
        Color toUse = g.getColor();
        if (isDerived()) g.dashed(shape, g.context.fill(), toUse);
        else g.draw(shape, g.context.fill(), toUse);
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
            Line2D shape = g.lineUnderString(getName(), 0, 3);
            if (parent.isWeak()) g.dashed(shape);
            else drawShape(g, shape);
        }
        if (g.getContext().drawDebugInfo()) {
            g.scale(0.7, 0.7);
            g.drawStringCenter(String.valueOf(dataType), 0, -12);
        }
    }

    @Override
    public Shape getShape(double width, double height) {
        // Ensure at least 7 px per character
        double newWidth = Math.max(width * 0.7, UNIVERSAL_METRICS.stringWidth(getName()) * 1.05f);
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

    @Override
    public Attribute clone() {
        Attribute clone = new Attribute();
        clone.setName(getName());
        clone.setWeak(isWeak());
        attributes.forEach(a -> clone.addAttribute(a.clone()));
        clone.x = x;
        clone.y = y;
        clone.setDataType(getDataType());
        clone.setKey(isKey());
        clone.setDerived(isDerived());
        clone.setParent(getParent());
        return clone;
    }
}
