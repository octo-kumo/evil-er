package model.er;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import shapes.lines.RelationLine;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class Specialization extends Relationship {
    @Expose
    private boolean disjoint = false;

    public Specialization() {
        super();
    }

    public Specialization(Entity superclass) {
        super();
        setSuperclass(superclass);
    }

    public Entity getSuperclass() {
        return nodes != null && nodes.size() > 0 ? nodes.get(0) : null;
    }

    public void setSuperclass(Entity superclass) {
        HAS_NODE_CHANGED = true;
        if (superclass == null) {
            nodes.clear();
            specs.clear();
            lines.clear();
            return;
        }
        set(superclass.add(0, 50));

        if (nodes.size() > 0) {
            nodes.set(0, superclass);
            lines.set(0, new RelationLine(this, superclass, specs.get(0)));
        } else {
            nodes.add(superclass);
            RelationshipSpec spec;
            specs.add(spec = new RelationshipSpec());
            lines.add(new RelationLine(this, superclass, spec));
            revalidate();
        }
        setName("Family Tree: " + (getSuperclass() == null ? "null" : getSuperclass().getName()));
    }

    public void addNode(Entity subclass, RelationshipSpec spec) {
        if (nodes.indexOf(subclass) == 0) return; // cant replace super
        super.addNode(subclass, spec);
    }

    public boolean remove(int index) {
        if (index == 0) return false; // cant remove super
        return super.remove(index);
    }

    public String getName() {
        return isDisjoint() ? "d" : "o";
    }

    public boolean isDisjoint() {
        return disjoint;
    }

    public void setDisjoint(boolean disjoint) {
        HAS_NODE_CHANGED = true;
        this.disjoint = disjoint;
    }

    @Override
    public Shape getShape(double width, double height) {
        return new Ellipse2D.Double(-height * .5 / 2d, -height * .5 / 2d, height * .5, height * .5);
    }

    public boolean hasSubclass(Entity subclass) {
        return nodes.indexOf(subclass) > 0;
    }

    public List<Entity> getSubclasses() {
        return nodes.subList(1, nodes.size());
    }

    public void drawShape(DiagramGraphics g, Shape shape) {
        Color toUse = g.getColor();
        if (nodes.size() == 2) {
            if (g.context.exporting()) return;
            g.dashed(shape, g.context.fill(), toUse);
        } else g.draw(shape, g.context.fill(), toUse);
    }

    @Override
    public void paint(DiagramGraphics g) {
        attributes.forEach(a -> a.draw(g));
        drawShape(g);
        if (nodes.size() == 2 && g.context.exporting()) return;
        g.drawStringCenter(getName(), 0, 0);
    }

    @Override
    public Specialization clone() {
        Specialization clone = new Specialization();
        clone.id = id;
        clone.setName(getName());
        clone.setWeak(isWeak());
        attributes.forEach(a -> clone.addAttribute(a.clone()));
        clone.set(this);

        for (int i = 0; i < specs.size(); i++) clone.addNode(nodes.get(i), specs.get(i).clone());

        clone.setDisjoint(isDisjoint());
        return clone;
    }
}
