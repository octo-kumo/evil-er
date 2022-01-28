package model.entities;

import model.Node;
import model.Vector;
import main.renderer.DiagramGraphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static main.renderer.DiagramGraphics.flatten;

public class Entity extends Node {
    public static Color HIGHLIGHTED = new Color(230, 230, 230);
    public static final double WIDTH = 100;
    public static final double HEIGHT = 40;
    public static final double INNER_WIDTH = WIDTH - 6;
    public static final double INNER_HEIGHT = HEIGHT - 6;

    public String name;
    public boolean weak;
    public boolean highlighted = false;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public ArrayList<Attribute> attributes;

    public Entity() {
        attributes = new ArrayList<>();
    }

    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute.setParent(this));
    }

    public void removeAttribute(Attribute attribute) {
        this.attributes.remove(attribute.setParent(null));
    }

    public Entity setName(String name) {
        this.name = name;
        return this;
    }

    public Entity setWeak(boolean weak) {
        this.weak = weak;
        return this;
    }

    @Override
    public void prepaint(DiagramGraphics g) {
        attributes.forEach(a -> a.predraw(g));
    }

    @Override
    public void paint(DiagramGraphics g) {
        attributes.forEach(a -> a.draw(g));
        drawShape(g);
        g.drawStringCenter(name, 0, 0);
    }

    public void drawShape(DiagramGraphics g) {
        drawShape(g, getShape());
        if (weak) drawShape(g, getShape(INNER_WIDTH, INNER_HEIGHT));
    }

    public void drawShape(DiagramGraphics g, Shape shape) {
        Color toUse = g.getColor();
        g.draw(shape, highlighted ? HIGHLIGHTED : Color.WHITE, toUse);
    }

    public Shape getShape() {
        return getShape(WIDTH, HEIGHT);
    }

    public Shape getShape(double width, double height) {
        return new Rectangle2D.Double(-width / 2d, -height / 2d, width, height);
    }

    public Shape getShapeWorld() {
        AffineTransform tx = new AffineTransform();
        tx.translate(getX(), getY());
        return tx.createTransformedShape(getShape());
    }

    public static double applyForces(List<? extends Entity> entities, Vector center, double pad) {
        entities = flatten(entities).toList();

        int len = entities.size();
        Vector[] forces = IntStream.range(0, len).mapToObj(i -> new Vector()).toArray(Vector[]::new);

        for (int a = 0; a < len; a++) {
            Entity A = entities.get(a);
            boolean aIsAttr = A instanceof Attribute;
            boolean aIsRel = A instanceof Relationship;

            Vector diff = (aIsAttr ? ((Attribute) A).parent.pos() : center).minus(A.pos());
            diff.x *= Entity.HEIGHT / Entity.WIDTH;

            Vector force = force(diff, Entity.A, B, pad);
            forces[a].incre(force);

            for (int b = 0; b < len; b++) {
                if (a == b) continue;
//                if(a instanceof Attribute){
                Entity B = entities.get(b);
                diff = B.pos().minus(A.pos());
                diff.x *= Entity.HEIGHT / Entity.WIDTH;

                boolean bIsAttr = B instanceof Attribute;
                boolean bIsRel = B instanceof Relationship;

                boolean bothAttr = aIsAttr && bIsAttr;
                boolean anyAttr = aIsAttr || bIsAttr;
                boolean onlyA = aIsAttr && !bothAttr;
                boolean onlyB = bIsAttr && !bothAttr;

                force = force(diff, Entity.A, Entity.B, bothAttr ? pad / 1.5 : anyAttr ? pad / 1.3 : pad);
                if (force.dot(B.pos().minus(A.pos())) > 0) {
                    // skip attribute attraction between attributes of different entities
                    if (bothAttr && ((Attribute) A).parent != ((Attribute) B).parent) continue;
                    // skip entity attraction
                    if (!(aIsRel | aIsAttr) && !(bIsRel | bIsAttr)) continue;
                }
                // skip force on non attributes, by attributes
                if (!onlyB) forces[a].incre(force);
                if (!onlyA) forces[b].decre(force);
            }
        }

        double total = 0;
        for (int i = 0; i < len; i++) {
            Entity a = entities.get(i);
            a.pos(a.pos().incre(forces[i]));
            total += forces[i].len();
        }

        return total;
    }

    public void pos(Vector n) {
        this.x = n.x;
        this.y = n.y;
    }

    public Vector pos() {
        return new Vector(this.getX(), this.getY());
    }

    static final double A = 0.5;
    static final double B = 1;

    public static Vector force(Vector diff, double A, double B, double R) {
        double r = diff.len() / R;
        Vector n = diff.norm();
        if (r == 0) return new Vector();
        if (r > 3) r = 3;
        double v = A / Math.pow(r, 4) - B / Math.pow(r, 2);
        return n.scale(-v * 0.1).cap(10);
    }

    public static Vector force(Vector diff) {
        return force(diff, A, B, 100);
    }

    public String toString() {
        return String.format("<%s : %.2f, %.2f>", getClass().getSimpleName(), getX(), getY());
    }
}
