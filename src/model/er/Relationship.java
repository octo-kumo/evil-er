package model.er;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import shapes.Diamond;
import shapes.lines.RelationLine;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Relationship<T extends Entity> extends Entity {
    public List<Entity> nodes;
    @Expose
    public List<RelationshipSpec> specs;
    public List<RelationLine<T>> lines;

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
        nodes.add(node);
        specs.add(spec);
        lines.add(new RelationLine<>(this, node, spec));
        revalidate();
    }

    public boolean remove(int index) {
        if (index >= nodes.size()) return false;
        nodes.remove(index);
        specs.remove(index);
        lines.remove(index);
        revalidate();
        return true;
    }

    public void remove(Entity entity) {
        int index = nodes.indexOf(entity);
        if (index != -1) remove(index);
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

    public void revalidate() {
        HashMap<Entity, Integer> map = new HashMap<>();
        for (int i = 0; i < specs.size(); i++) {
            Entity key = nodes.get(i);
            RelationshipSpec spec = specs.get(i);
            spec.index = i;
            map.putIfAbsent(key, 0);
            map.computeIfPresent(key, (entity, integer) -> (spec.uniqueIndex = integer) + 1);
        }
        for (int i = 0; i < specs.size(); i++)
            specs.get(i).dupeCount = map.getOrDefault(nodes.get(i), 0);
    }

    public static class RelationshipSpec {
        @Expose
        public String amm;
        @Expose
        public String role;
        @Expose
        public boolean total;

        public int index;
        public int uniqueIndex;
        public int dupeCount;

        public RelationshipSpec() {
            this("", false, "");
        }

        public RelationshipSpec(boolean total) {
            this("", total, "");
        }

        public RelationshipSpec(String amm, boolean total) {
            this(amm, total, "");
        }

        public RelationshipSpec(String amm, boolean total, String role) {
            this.amm = amm;
            this.total = total;
            this.role = role;
        }
    }
}
