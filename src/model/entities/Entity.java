package model.entities;

import com.google.gson.annotations.Expose;
import model.Node;
import model.Vector;
import main.renderer.DiagramGraphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static main.renderer.DiagramGraphics.flatten;

public class Entity extends Node {
    public static void updateParents(Entity entity) {
        entity.attributes.forEach(a -> {
            a.setParent(entity);
            updateParents(a);
        });
    }

    public String getName() {
        return name;
    }

    public boolean isWeak() {
        return weak;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public enum Types {
        Select, Entity, Relationship, Attribute
    }

    public static Color HIGHLIGHTED = new Color(230, 230, 230);
    public static final double WIDTH = 100;
    public static final double HEIGHT = 40;
    public static final double INNER_WIDTH = WIDTH - 6;
    public static final double INNER_HEIGHT = HEIGHT - 6;
    @Expose
    private String name;
    @Expose
    private boolean weak;
    @Expose
    private boolean highlighted = false;
    @Expose
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
        g.drawStringCenter(getName(), 0, 0);
    }

    public void drawShape(DiagramGraphics g) {
        drawShape(g, getShape());
        if (isWeak()) drawShape(g, getShape(INNER_WIDTH, INNER_HEIGHT));
    }

    public void drawShape(DiagramGraphics g, Shape shape) {
        Color toUse = g.getColor();
        g.draw(shape, isHighlighted() ? HIGHLIGHTED : Color.WHITE, toUse);
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

    public static double applyForces(List<? extends Entity> entities, double pad) {
        List<Entity> objects = flatten(entities).collect(Collectors.toList());

        int len = objects.size();
        Vector[] forces = IntStream.range(0, len).mapToObj(i -> new Vector()).toArray(Vector[]::new);
        IntStream.range(0, len).forEach(a -> {
            Entity A = objects.get(a);
            boolean aIsAttr = A instanceof Attribute;
            boolean aIsRel = A instanceof Relationship;

            if (aIsAttr) {
                Vector diff = A.minus(((Attribute) A).getParent());
                Vector force = force(diff, Entity.A, B, pad);
                forces[a].incre(force);
            }
            IntStream.range(0, len).forEach(b -> {
                Entity B = objects.get(b);
                Vector diff = B.minus(A);

                boolean bIsAttr = B instanceof Attribute;
                boolean bIsRel = B instanceof Relationship;

                Vector force = force(diff, Entity.A, Entity.B, aIsAttr && bIsAttr ? pad / 1.5 : aIsAttr || bIsAttr ? pad / 1.3 : pad);
                if (force.dot(diff) > 0) {
                    // skip attribute attraction between attributes of different entities
                    if (aIsAttr && bIsAttr && ((Attribute) A).getParent() != ((Attribute) B).getParent()) return;
                    // skip entity attraction
                    if (!aIsRel && !aIsAttr && !bIsRel && !bIsAttr) return;
                }
                // skip force on non attributes, by attributes
                if (!bIsAttr || aIsAttr) forces[a].incre(force);
                if (!aIsAttr || bIsAttr) forces[b].decre(force);
            });
        });

        double total = 0;
        for (int i = 0; i < len; i++) {
            Entity a = objects.get(i);
            total += forces[i].len();
            a.incre(forces[i]);
        }

        return total;
    }

    static final double A = 0.5;
    static final double B = 1;

    public static Vector force(Vector diff, double A, double B, double R) {
        diff = diff.multi(Entity.HEIGHT / Entity.WIDTH, 1);
        double r = diff.len() / R;
        Vector n = diff.norm();
        if (r == 0) return new Vector();
        if (r > 3) r = 3;
        double v = A / Math.pow(r, 4) - B / Math.pow(r, 2);
        return n.scale(-v * 0.2).cap(10);
    }

    public static Vector force(Vector diff) {
        return force(diff, A, B, 100);
    }

    public String toString() {
        return String.format("<%s : %.2f, %.2f>", getClass().getSimpleName(), getX(), getY());
    }
}
