package model.lines;

import model.Node;
import model.entities.Relationship;
import shapes.DoubleLine;

import java.awt.*;

public class RelationLine<T extends Node> extends Line<Relationship<T>, T> {
    public Relationship.RelationshipSpec spec;

    public RelationLine(Relationship<T> nodeA, T nodeB, Relationship.RelationshipSpec spec) {
        super(nodeA, nodeB);
        this.spec = spec;
    }

    @Override
    public void predraw(Graphics2D g) {
        this.draw(g);
    }

    @Override
    public void draw(Graphics2D g) {
        if (spec.total()) g.draw(new DoubleLine(a.x, a.y, b.x, b.y));
        else g.drawLine(a.x, a.y, b.x, b.y);
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        dx /= 2;
        dy /= 2;
        g.drawString(spec.many() ? "N" : "1", (int) (a.x + dx) + 5, (int) (a.y + dy));
    }
}
