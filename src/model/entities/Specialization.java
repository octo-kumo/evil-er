package model.entities;

import com.google.gson.annotations.Expose;
import model.lines.RelationLine;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Specialization extends Relationship<Entity> {
    @Expose
    private boolean disjointSpecialization = false;

    public Specialization(Entity superclass) {
        setSuperclass(superclass);
    }

    public Entity getSuperclass() {
        return nodes.size() > 0 ? nodes.get(0) : null;
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
        else getSuperclass().setX(y - this.y);
    }

    public void setSuperclass(Entity superclass) {
        nodes.set(0, superclass);
        lines.set(0, new RelationLine<>(this, superclass,
                specs.set(0, new RelationshipSpec(true))));
        setName("Family Tree: " + superclass.getName());
    }

    public void addSubclass(Entity subclass, RelationshipSpec spec) {
        if (nodes.contains(subclass)) {
            int i = nodes.indexOf(subclass);
            if (i == 0) return; // ????
            spec.subset = true;
            nodes.set(i, subclass);
            specs.set(i, spec);
            lines.set(i, new RelationLine<>(this, subclass, spec));
            return;
        }
        spec.subset = true;
        nodes.add(subclass);
        specs.add(spec);
        lines.add(new RelationLine<>(this, subclass, spec));
    }

    public String getName() {
        return isDisjointSpecialization() ? "d" : "o";
    }

    public boolean isDisjointSpecialization() {
        return disjointSpecialization;
    }

    @Override
    public Shape getShape(double width, double height) {
        return new Ellipse2D.Double(-height / 2d, -height / 2d, height, height);
    }

    public void setDisjointSpecialization(boolean disjointSpecialization) {
        this.disjointSpecialization = disjointSpecialization;
    }
}
