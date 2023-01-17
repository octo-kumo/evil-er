package model.er;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import model.Vector;
import model.rs.Column;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import static main.er.ERDiagram.UNIVERSAL_METRICS;

public class Attribute extends Entity {
    private Entity parent;
    @Expose
    private boolean key;
    @Expose
    private boolean derived;
    @Expose
    private boolean unique;
    @Expose
    private Column.DataType dataType;
    @Expose
    private String dataParam;

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
            g.drawStringCenter(getDataType() + "(" + getDataParam() + ")", 0, -12);
        }
    }

    @Override
    public double getX() {
        return x + (getParent() == null ? 0 : getParent().getX());
    }

    @Override
    public void setX(double x) {
        super.setX(x - (getParent() == null ? 0 : getParent().getX()));
    }

    @Override
    public double getY() {
        return y + (getParent() == null ? 0 : getParent().getY());
    }

    @Override
    public void setY(double y) {
        super.setY(y - (getParent() == null ? 0 : getParent().getY()));
    }

    public Column.DataType getDataType() {
        return dataType;
    }

    public void setDataType(Column.DataType dataType) {
        this.dataType = dataType;
        HAS_NODE_CHANGED = true;
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
        HAS_NODE_CHANGED = true;
        this.key = key;
        return this;
    }

    public boolean isDerived() {
        return derived;
    }

    public Attribute setDerived(boolean derived) {
        HAS_NODE_CHANGED = true;
        this.derived = derived;
        return this;
    }

    public Entity getParent() {
        return parent;
    }

    public Attribute setParent(Entity parent) {
        HAS_NODE_CHANGED = true;
        this.parent = parent;
        return this;
    }

    public String getDataParam() {
        return dataParam;
    }

    public void setDataParam(String dataParam) {
        HAS_NODE_CHANGED = true;
        this.dataParam = dataParam;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        HAS_NODE_CHANGED = true;
        this.unique = unique;
    }

    @Override
    public Attribute clone() {
        Attribute clone = new Attribute();
        clone.setName(getName());
        clone.setWeak(isWeak());
        attributes.forEach(a -> clone.addAttribute(a.clone()));
        clone.id = id;
        clone.x = x;
        clone.y = y;
        clone.setUnique(isUnique());
        clone.setDataParam(getDataParam());
        clone.setDataType(getDataType());
        clone.setKey(isKey());
        clone.setDerived(isDerived());
        clone.setParent(getParent());
        return clone;
    }
}
