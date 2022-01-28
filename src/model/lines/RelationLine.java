package model.lines;

import model.Node;
import model.entities.Relationship;
import main.renderer.DiagramGraphics;
import shapes.FancyLine;

import java.awt.*;

public class RelationLine<T extends Node> extends Line<Relationship<T>, T> {
    public Relationship.RelationshipSpec spec;

    private static final BasicStroke base = new BasicStroke(4, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private static final BasicStroke center = new BasicStroke(2.8f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

    public RelationLine(Relationship<T> nodeA, T nodeB, Relationship.RelationshipSpec spec) {
        super(nodeA, nodeB);
        this.spec = spec;
    }

    @Override
    public void predraw(DiagramGraphics g) {
        FancyLine line = new FancyLine(a.getX(), a.getY(), b.getX(), b.getY(), g.getContext().getStyle());
        Color toUse = g.getColor();
        if (spec.total) {
            Stroke stroke = g.getStroke();
            g.setStroke(base);
            g.draw(line);
            g.setStroke(center);
            g.setColor(Color.WHITE);
            g.draw(line);
            g.setStroke(stroke);
        } else g.draw(line);
        g.setColor(toUse);
    }

    @Override
    public void draw(DiagramGraphics g) {
        double cx = (b.getX() + a.getX()) / 2;
        double cy = (b.getY() + a.getY()) / 2;
        double dist = 10;
        double angle = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX()) + Math.PI / 2;
        if (angle > Math.PI) angle -= 2 * Math.PI;
        if (angle < -Math.PI) angle += 2 * Math.PI;
        if (angle > 0) angle -= Math.PI;
        g.drawStringCenter(spec.amm, (float) (cx + Math.cos(angle) * dist), (float) (cy + Math.sin(angle) * dist));
    }
}
