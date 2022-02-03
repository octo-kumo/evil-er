package model.entities;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import model.lines.Line;
import model.lines.RelationLine;
import shapes.Diamond;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Relationship<T extends Entity> extends Entity {
    public List<Entity> nodes;
    @Expose
    public List<RelationshipSpec> specs;
    public List<Line<Relationship<T>, T>> lines;

    /**
     * Used in deserialization when only the name is supplied
     */
    public String[] _nodes;

    public Relationship() {
        nodes = new ArrayList<>();
        specs = new ArrayList<>();
        lines = new ArrayList<>();
    }

    public Relationship(String name) {
        this();
        setName(name);
    }

    public void addNode(T node, RelationshipSpec spec) {
        if (nodes.contains(node)) {
            int i = nodes.indexOf(node);
            nodes.set(i, node);
            specs.set(i, spec);
            lines.set(i, new RelationLine<>(this, node, spec));
            return;
        }
        nodes.add(node);
        specs.add(spec);
        lines.add(new RelationLine<>(this, node, spec));
    }

    public boolean remove(int index) {
        if (index >= nodes.size()) return false;
        nodes.remove(index);
        specs.remove(index);
        lines.remove(index);
        return true;
    }

    public boolean remove(Entity entity) {
        int index = nodes.indexOf(entity);
        if (index == -1) return false;
        else return remove(index);
    }

    @Override
    public void predraw(DiagramGraphics g) {
        super.predraw(g);
        lines.forEach(l -> l.predraw(g));
        lines.forEach(l -> l.draw(g));
    }

    public void drawShape(DiagramGraphics g) {
        drawShape(g, getShape());
        if (isWeak()) drawShape(g, getShape(WIDTH - 15, INNER_HEIGHT));
    }

    @Override
    public Shape getShape(double width, double height) {
        return new Diamond(-width / 2d, -height / 2d, width, height);
    }

    public static class RelationshipSpec {
        @Expose
        public String amm;
        @Expose
        public boolean total;

        public RelationshipSpec() {
            this("", false);
        }


        public RelationshipSpec(boolean total) {
            this("", total);
        }

        public RelationshipSpec(String amm, boolean total) {
            this.amm = amm;
            this.total = total;
        }
    }
}
