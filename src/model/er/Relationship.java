package model.er;

import com.google.gson.annotations.Expose;
import main.renderer.DiagramGraphics;
import shapes.Diamond;
import shapes.lines.RelationLine;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static main.er.ERDiagram.UNIVERSAL_METRICS;

public class Relationship extends Entity {
    @Expose
    private boolean associative;
    public List<Entity> nodes;
    @Expose
    public List<RelationshipSpec> specs;
    public List<RelationLine> lines;

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

    public void addNode(Entity node, RelationshipSpec spec) {
        HAS_NODE_CHANGED = true;
        nodes.add(node);
        specs.add(spec);
        lines.add(new RelationLine(this, node, spec));
        revalidate();
    }

    public boolean set(int index, Entity node) {
        if (index >= nodes.size()) return false;
        HAS_NODE_CHANGED = true;
        nodes.set(index, node);
        lines.get(index).b = node;
        revalidate();
        return true;
    }

    public boolean remove(int index) {
        if (index >= nodes.size()) return false;
        HAS_NODE_CHANGED = true;
        nodes.remove(index);
        specs.remove(index);
        lines.remove(index);
        revalidate();
        return true;
    }

    public void remove(Entity entity) {
        HAS_NODE_CHANGED = true;
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
        double newWidth = Math.max(WIDTH, UNIVERSAL_METRICS.stringWidth(getName()) * 1.05f);
        if (isAssociative()) drawShape(g, new Rectangle2D.Double(-newWidth / 2d, -HEIGHT / 2d, newWidth, HEIGHT));

        drawShape(g, getShape());
        // Ensure at least 7 px per character
        if (isWeak()) drawShape(g, getShape(newWidth - 15, INNER_HEIGHT));
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

    public Relationship clone() {
        Relationship clone = new Relationship();
        clone.id = id;
        clone.setName(getName());
        clone.setWeak(isWeak());
        attributes.forEach(a -> clone.addAttribute(a.clone()));
        clone.set(this);

        for (int i = 0; i < specs.size(); i++) clone.addNode(nodes.get(i), specs.get(i).clone());
        return clone;
    }

    public boolean isAssociative() {
        return associative;
    }

    public void setAssociative(boolean associative) {
        this.associative = associative;
    }

    public static class RelationshipSpec {
        @Expose
        private String amm;
        @Expose
        private String role;
        @Expose
        private boolean total;
        @Expose
        private boolean optional;

        private int index;
        private int uniqueIndex;
        private int dupeCount;

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

        @Override
        public RelationshipSpec clone() {
            return new RelationshipSpec(amm, total, role);
        }

        public String getAmm() {
            return amm;
        }

        public void setAmm(String amm) {
            HAS_NODE_CHANGED = true;
            this.amm = amm;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            HAS_NODE_CHANGED = true;
            this.role = role;
        }

        public boolean isTotal() {
            return total;
        }

        public void setTotal(boolean total) {
            HAS_NODE_CHANGED = true;
            this.total = total;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            HAS_NODE_CHANGED = true;
            this.index = index;
        }

        public int getUniqueIndex() {
            return uniqueIndex;
        }

        public void setUniqueIndex(int uniqueIndex) {
            HAS_NODE_CHANGED = true;
            this.uniqueIndex = uniqueIndex;
        }

        public int getDupeCount() {
            return dupeCount;
        }

        public void setDupeCount(int dupeCount) {
            HAS_NODE_CHANGED = true;
            this.dupeCount = dupeCount;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            HAS_NODE_CHANGED = true;
            this.optional = optional;
        }
    }
}
