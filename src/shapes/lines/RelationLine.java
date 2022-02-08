package shapes.lines;

import main.renderer.DiagramGraphics;
import model.Vector;
import model.er.Entity;
import model.er.Relationship;
import model.er.Specialization;
import shapes.SubsetSymbol;

import java.awt.*;

public class RelationLine<T extends Entity> extends Line<Relationship<T>, T> {
    public Relationship.RelationshipSpec spec;

    private final Vector normal = new Vector();

    private static final BasicStroke base = new BasicStroke(4, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private static final BasicStroke center = new BasicStroke(2.8f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

    public RelationLine(Relationship<T> nodeA, T nodeB, Relationship.RelationshipSpec spec) {
        super(nodeA, nodeB);
        this.spec = spec;
    }

    @Override
    public void predraw(DiagramGraphics g) {
        setNormal();
        Vector a = getStart();
        Vector b = getEnd();
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
            double dis = g.getContext().getLineStyle() == LineStyle.STRAIGHT ? 0.6 : 0.5;
            Vector mid = b.multi(1 - dis).add(a.multi(dis));
            g.draw(new SubsetSymbol(mid, diff, 8, g.getContext().getLineStyle()));
        }
    }

    @Override
    public void draw(DiagramGraphics g) {
        Vector a = getStart();
        Vector b = getEnd();

        Vector d = b.minus(a);
        Vector c = a.add(b).div(2);
        if (!spec.role.isEmpty()) {
            Vector mid = b.add(a).div(2);
            double angle = Vector.alwaysUp(d.angle() - Math.PI / 2) + Math.PI / 2;
            g.rotate(angle, mid.getX(), mid.getY());
            g.drawStringCenter(spec.role, mid, Color.WHITE);
            g.rotate(-angle, mid.getX(), mid.getY());
        }
        if (!spec.amm.isEmpty()) {
            double dist = 10;
            double angle = Vector.alwaysUp(d.angle() + Math.PI / 2) + (spec.uniqueIndex % 2 == 0 ? 0 : Math.PI);
            g.drawStringCenter(spec.amm, c.add(Math.cos(angle) * dist, Math.sin(angle) * dist));
        }
    }

    public Vector getStart() {
        return spec.uniqueIndex == 0 ? spec.dupeCount % 2 == 0 ? a.minus(normal.multi(.5)) : a :
                a.add(normal.multi(bouncingIndex(spec.uniqueIndex) + (spec.dupeCount % 2 == 0 ? -.5 : 0)));
    }

    public Vector getEnd() {
        return spec.uniqueIndex == 0 ? spec.dupeCount % 2 == 0 ? b.minus(normal.multi(.5)) : b :
                b.add(normal.multi(bouncingIndex(spec.uniqueIndex) + (spec.dupeCount % 2 == 0 ? -.5 : 0)));
    }

    public void setNormal() {
        normal.set(b.minus(a).rot90().norm().scale(15));
    }

    public static int bouncingIndex(int i) {
        return i % 2 == 0 ? -i / 2 : i / 2 + 1;
    }
}
