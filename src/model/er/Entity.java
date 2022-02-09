package model.er;

import com.google.gson.annotations.Expose;
import model.Vector;
import main.renderer.DiagramGraphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static main.er.ERDiagram.UNIVERSAL_METRICS;
import static main.renderer.DiagramGraphics.flatten;

public class Entity extends Node {
    public Entity(String name) {
        this();
        setName(name);
    }

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

    public enum Type {
        Select, Entity, Relationship, Attribute, Specialization
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
        // Ensure at least 7 px per character
        double newWidth = Math.max(WIDTH, UNIVERSAL_METRICS.stringWidth(getName()) * 1.05f);
        if (isWeak()) drawShape(g, getShape(newWidth - 6, INNER_HEIGHT));
    }

    public void drawShape(DiagramGraphics g, Shape shape) {
        Color toUse = g.getColor();
        g.draw(shape, isHighlighted() ? HIGHLIGHTED : Color.WHITE, toUse);
    }

    public Shape getShape() {
        // Ensure at least 7 px per character
        double newWidth = Math.max(WIDTH, UNIVERSAL_METRICS.stringWidth(getName()) * 1.05f);
        return getShape(newWidth, HEIGHT);
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
        Vector com = objects.stream().map(e -> (Vector) e).reduce(Vector::add).orElse(Vector.ZERO).div(len);
        Vector[] forces = IntStream.range(0, len).mapToObj(i -> new Vector()).toArray(Vector[]::new);
        IntStream.range(0, len).forEach(a -> {
            Entity A = objects.get(a);

            if (A.getClass() != Attribute.class) {
                Vector diff = A.minus(com);
                forces[a].incre(diff.log().scale(-1).cap(1));
            }
            IntStream.range(0, len).forEach(b -> {
                Entity B = objects.get(b);
                if (shouldSkip(A, B) || shouldSkip(B, A)) return;

                Vector diff = B.minus(A);
                double p;
                if ((p = shouldAttract(objects, A, B)) != 0 || (p = shouldAttract(objects, A, B)) != 0) {
                    Vector force = attract(diff, pad * p, 1);
                    forces[a].incre(force);
                    forces[b].incre(force.neg());
                } else if (A instanceof Relationship && B instanceof Relationship) {
                    Vector force = repulsion(diff, pad * 2, 0.1);
                    forces[a].incre(force);
                    forces[b].incre(force.neg());
                } else {
                    Vector force = repulsion(diff, pad, 0.001);
                    forces[a].incre(force);
                    forces[b].incre(force.neg());
                }
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

    public static boolean shouldSkip(Entity a, Entity b) {
        return a instanceof Specialization && ((Specialization) a).getSuperclass() == b;
    }

    public static double shouldAttract(List<Entity> entities, Entity a, Entity b) {

        if (a.getClass() == Entity.class && b.getClass() == Entity.class) {
            boolean related = entities.stream().anyMatch(e -> e.getClass() == Relationship.class &&
                    ((Relationship<?>) e).nodes.contains(a) && ((Relationship<?>) e).nodes.contains(b));

            boolean shareSubclass = entities.stream().anyMatch(e -> e.getClass() == Specialization.class &&
                    ((Relationship<?>) e).nodes.indexOf(a) > 0 && ((Relationship<?>) e).nodes.indexOf(b) > 0);

            return shareSubclass ? 0.8 : related ? 1.5 : 3; // all entities attract
        }
        if (b instanceof Attribute)
            return ((Attribute) b).getParent() == a || // only attract to parent or if same parent
                    (a instanceof Attribute && ((Attribute) a).getParent() == ((Attribute) b).getParent()) ? 0.5 : 0;
        if (a.getClass() == Entity.class && b instanceof Specialization)
            return ((Specialization) b).nodes.indexOf(a) > 0 ? 0.6 : 0; // not super class and also in the set
        if (a.getClass() == Entity.class && b instanceof Relationship)
            return ((Relationship<?>) b).nodes.contains(a) ? 1 : 0; // not specialization, hence just check if in

        return 0;
    }

    public static Vector attract(Vector diff, double R, double p) {
        diff = diff.multi(Entity.HEIGHT / Entity.WIDTH, 1);
        double len = diff.len();
        if (len > R * 3) return Vector.ZERO;
        double f = p * Math.pow(Math.log(ensureNonZero(len) / R), 3);
        return diff.norm().scale(f).cap(10);
    }

    public static Vector repulsion(Vector diff, double R, double p) {
        diff = diff.multi(Entity.WIDTH / Entity.HEIGHT, 1);
        double len = diff.len();
        if (len > R) return Vector.ZERO;
        double f = -p / Math.pow(Math.log(ensureNonZero(len) / R + 1), 2);
        return diff.norm().scale(f).cap(1);
    }

    public static double ensureNonZero(double num) {
        if (num == 0) return 1;
        return num;
    }

    public String toString() {
        return String.format("<%s : %.2f, %.2f>", getClass().getSimpleName(), getX(), getY());
    }
}
