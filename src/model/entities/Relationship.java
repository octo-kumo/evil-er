package model.entities;

import model.lines.Line;
import model.Node;
import model.lines.RelationLine;
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
        nodes.add(node);
        specs.add(spec);
        lines.add(new RelationLine<T>(this, node, spec));
        return this;
    }

    @Override
    public void predraw(Graphics2D g) {
        super.predraw(g);
        lines.forEach(l -> l.draw(g));
    }

    @Override
    public void drawShape(Graphics2D g) {
        Node.draw(g, new Diamond(-WIDTH / 2d, -HEIGHT / 2d, WIDTH, HEIGHT));
        if (weak) Node.draw(g, new Diamond(-INNER_WIDTH / 2d, -INNER_HEIGHT / 2d, INNER_WIDTH, INNER_HEIGHT));
    }

    public static record RelationshipSpec(boolean many, boolean total) {
    }
}
