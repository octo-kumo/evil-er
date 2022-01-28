package model.entities;

import model.lines.Line;
import model.Node;
import model.lines.RelationLine;
import main.renderer.DiagramGraphics;
import shapes.Diamond;

import java.awt.*;
import java.util.ArrayList;

public class Relationship<T extends Node> extends Entity {
    public ArrayList<T> nodes;
    public ArrayList<RelationshipSpec> specs;
    public ArrayList<Line<Relationship<T>, T>> lines;

    public Relationship() {
        nodes = new ArrayList<>();
        specs = new ArrayList<>();
        lines = new ArrayList<>();
    }

    public Relationship<T> addNode(T node, RelationshipSpec spec) {
        if (nodes.contains(node)) {
            int i = nodes.indexOf(node);
            nodes.set(i, node);
            specs.set(i, spec);
            lines.set(i, new RelationLine<>(this, node, spec));
            return this;
        }
        nodes.add(node);
        specs.add(spec);
        lines.add(new RelationLine<>(this, node, spec));
        return this;
    }

    public void remove(int index) {
        if (index >= nodes.size()) return;
        nodes.remove(index);
        specs.remove(index);
        lines.remove(index);
    }

    public void remove(Entity entity) {
        @SuppressWarnings("unchecked")
        int index = nodes.indexOf((T) entity);
        if (index != -1) {
            remove(index);
        }
    }

    @Override
    public void predraw(DiagramGraphics g) {
        super.predraw(g);
        lines.forEach(l -> l.predraw(g));
        lines.forEach(l -> l.draw(g));
    }

    public void drawShape(DiagramGraphics g) {
        drawShape(g, getShape());
        if (weak) drawShape(g, getShape(WIDTH - 15, INNER_HEIGHT));
    }

    @Override
    public Shape getShape(double width, double height) {
        return new Diamond(-width / 2d, -height / 2d, width, height);
    }

    public static class RelationshipSpec {
        public String amm;
        public boolean total;

        public RelationshipSpec(String amm, boolean total) {
            this.amm = amm;
            this.total = total;
        }
    }
}
