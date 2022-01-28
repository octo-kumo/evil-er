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
        nodes.add(node);
        specs.add(spec);
        lines.add(new RelationLine<>(this, node, spec));
        return this;
    }

    @Override
    public void predraw(DiagramGraphics g) {
        super.predraw(g);
        lines.forEach(l -> l.predraw(g));
        lines.forEach(l -> l.draw(g));
    }

    @Override
    public Shape getShape(double width, double height) {
        return new Diamond(-width / 2d, -height / 2d, width, height);
    }

    public static record RelationshipSpec(boolean many, boolean total) {
    }
}
