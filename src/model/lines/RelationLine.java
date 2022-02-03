package model.lines;

import main.renderer.DiagramGraphics;
import model.Vector;
import model.entities.Entity;
import model.entities.Relationship;
import model.entities.Specialization;
import shapes.FancyLine;
import shapes.SubsetSymbol;

import java.awt.*;

public class RelationLine<T extends Entity> extends Line<Relationship<T>, T> {
    public Relationship.RelationshipSpec spec;

    private static final BasicStroke base = new BasicStroke(4, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private static final BasicStroke center = new BasicStroke(2.8f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

    public RelationLine(Relationship<T> nodeA, T nodeB, Relationship.RelationshipSpec spec) {
        super(nodeA, nodeB);
        this.spec = spec;
    }

    @Override
    public void predraw(DiagramGraphics g) {
        FancyLine line = new FancyLine(a, b, g.getContext().getLineStyle());
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
        if (a instanceof Specialization && b != ((Specialization) a).getSuperclass()) {
            Vector diff = b.minus(a);
            double dis = 0.4;
            Vector mid = b.multi(1 - dis).add(a.multi(dis));
            g.draw(new SubsetSymbol(mid, diff, 10, g.getContext().getLineStyle()));
        }
    }

    @Override
    public void draw(DiagramGraphics g) {
        Vector d = b.minus(a);
        Vector c = a.add(b).div(2);
        double dist = 10;
        double angle = d.angle() + Math.PI / 2;
        if (angle > Math.PI) angle -= 2 * Math.PI;
        if (angle < -Math.PI) angle += 2 * Math.PI;
        if (angle > 0) angle -= Math.PI;
        g.drawStringCenter(spec.amm, c.add(Math.cos(angle) * dist, Math.sin(angle) * dist));
    }
}
