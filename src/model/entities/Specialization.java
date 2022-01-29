package model.entities;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import model.lines.RelationLine;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Specialization extends Relationship<Entity> {
    @Expose
    private boolean disjointSpecialization = false;

    public Specialization(Entity superclass) {
        super();
        setSuperclass(superclass);
        x = 0;
        y = 50;
    }

    public Entity getSuperclass() {
        return nodes != null && nodes.size() > 0 ? nodes.get(0) : null;
    }

    public double getX() {
        return x + (getSuperclass() == null ? 0 : getSuperclass().getX());
    }

    public double getY() {
        return y + (getSuperclass() == null ? 0 : getSuperclass().getY());
    }

    public void setX(double x) {
        if (getSuperclass() == null) this.x = x;
        else getSuperclass().setX(x - this.x);
    }

    public void setY(double y) {
        if (getSuperclass() == null) this.y = y;
        else getSuperclass().setY(y - this.y);
    }

    public void setSuperclass(Entity superclass) {
        if (superclass == null) {
            nodes.clear();
            specs.clear();
            lines.clear();
            return;
        }

        x = 0;
        y = 50;
        if (nodes.size() > 0) {
            nodes.set(0, superclass);
            lines.set(0, new RelationLine<>(this, superclass,
                    specs.get(0)));
        } else {
            nodes.add(superclass);
            RelationshipSpec spec;
            specs.add(spec = new RelationshipSpec());
            lines.add(new RelationLine<>(this, superclass, spec));
        }
        setName("Family Tree: " + (getSuperclass() == null ? "null" : getSuperclass().getName()));
    }

    public void addNode(Entity subclass, RelationshipSpec spec) {
        if (nodes.indexOf(subclass) == 0) return; // cant replace super
        spec.subset = true;
        super.addNode(subclass, spec);
    }

    public boolean remove(int index) {
        if (index == 0) return false; // cant remove super
        return super.remove(index);
    }

    public String getName() {
        return isDisjointSpecialization() ? "d" : "o";
    }

    public boolean isDisjointSpecialization() {
        return disjointSpecialization;
    }

    public void drawShape(DiagramGraphics g) {
        if (getSuperclass() != null) g.translate(getSuperclass().getX(), getSuperclass().getY());
        super.drawShape(g);
    }

    @Override
    public Shape getShape(double width, double height) {
        return new Ellipse2D.Double(-height * .5 / 2d, -height * .5 / 2d, height * .5, height * .5);
    }

    public void setDisjointSpecialization(boolean disjointSpecialization) {
        this.disjointSpecialization = disjointSpecialization;
    }
}
