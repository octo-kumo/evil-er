package model.rs;

import main.renderer.DiagramGraphics;
import model.Drawable;
import model.Pair;
import model.Vector;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Table extends Vector implements Drawable {
    public String name;
    public Map<String, Attribute> attributeMap;

    private List<Attribute> sorted;
    private final List<Pair<Boolean, Table>> foreign;
    private boolean highlighted;
    public long keyCount = 0;
    public long colCount = 0;

    public Table(String name) {
        this.name = name;
        this.attributeMap = new HashMap<>();
        this.sorted = new ArrayList<>();
        this.foreign = new ArrayList<>();
    }

    public void add(Attribute attribute) {
        attribute.parent = this;
        this.attributeMap.put(attribute.name, attribute);
        revalidate();
    }

    public void add(Table other, boolean required) {
        this.foreign.add(new Pair<>(required, other));
        revalidate();
    }

    public void remove(Attribute attribute) {
        if (this.attributeMap.remove(attribute.name, attribute)) attribute.parent = null;
        revalidate();
    }

    public void revalidate() {
        this.sorted = attributeMap.values().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        for (int i = 0; i < this.sorted.size(); i++) this.sorted.get(i).index = i;
        keyCount = keys().count();
        colCount = sorted.size() + foreign.stream().mapToLong(t -> t.getB().keyCount).sum();
    }

    @Override
    public void draw(DiagramGraphics g) {
        AffineTransform transform = g.getTransform();
        g.translate(getX(), getY());
        g.drawStringCenter(name, (float) (Attribute.WIDTH / 2), (float) (Attribute.HEIGHT / 2));
        if (highlighted) g.draw(getShape());
        g.translate(0, Attribute.HEIGHT);
        for (Attribute e : sorted) {
            e.draw(g);
            g.translate(Attribute.WIDTH, 0);
        }
        g.setTransform(transform);
        int offset = 0;
        for (Pair<Boolean, Table> other : foreign) {
            other.getB().drawAsForeign(g, this.add((sorted.size() + offset) * Attribute.WIDTH, Attribute.HEIGHT));
            offset += other.getB().keyCount;
        }
    }

    @Override
    public void predraw(DiagramGraphics g) {
        int offset = 0;
        for (Pair<Boolean, Table> other : foreign) {
            other.getB().predrawAsForeign(g, this.add((sorted.size() + offset) * Attribute.WIDTH, Attribute.HEIGHT));
            offset += other.getB().keyCount;
        }
    }

    public void drawAsForeign(DiagramGraphics g, Vector origin) {
        selfKeys().forEach(e -> {
            e.drawAsForeign(g, origin);
            origin.incre(Attribute.WIDTH, 0);
        });
        foreign.stream().filter(Pair::getA).forEach(e -> e.getB().drawAsForeign(g, origin));
    }

    public void predrawAsForeign(DiagramGraphics g, Vector origin) {
        selfKeys().forEach(e -> {
            e.drawLine(g, origin);
            origin.incre(Attribute.WIDTH, 0);
        });
        foreign.stream().filter(Pair::getA).forEach(e -> e.getB().predrawAsForeign(g, origin));
    }

    public Stream<Attribute> selfKeys() {
        return sorted.stream().filter(e -> e.key);
    }

    public Stream<Attribute> keys() {
        return Stream.concat(sorted.stream().filter(e -> e.key),
                foreign.stream().filter(Pair::getA).flatMap(e -> e.getB().keys()));
    }

    public Rectangle2D getShapeWorld() {
        return new Rectangle2D.Double(getX(), getY(), colCount * Attribute.WIDTH, Attribute.HEIGHT * 2);
    }

    public Rectangle2D getShape() {
        return new Rectangle2D.Double(0, 0, colCount * Attribute.WIDTH, Attribute.HEIGHT * 2);
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
}
