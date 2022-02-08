package model.rs;

import main.renderer.DiagramGraphics;
import main.rs.RSDiagram;
import model.Drawable;
import model.Vector;
import model.others.Tuple;
import org.jetbrains.annotations.NotNull;
import shapes.lines.RangeLine;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Table extends Vector implements Drawable {
    public String name;
    public Map<String, Column> attributeMap;

    private List<Column> sorted;
    private final List<Tuple<Boolean, String, Table>> foreign;
    private boolean highlighted;
    public long keyCount = 0;
    public long colCount = 0;

    private List<Column> keys;
    private List<Column> cols;

    public Table(String name) {
        this.name = name;
        this.attributeMap = new HashMap<>();
        this.sorted = new ArrayList<>();
        this.foreign = new ArrayList<>();
    }

    public void add(@NotNull Column column) {
        column.parent = this;
        this.attributeMap.put(column.name, column);
        revalidate();
    }

    public void add(Table other, String name, boolean required) {
        this.foreign.add(new Tuple<>(required, name, other));
        revalidate();
    }

    public void remove(Column column) {
        if (this.attributeMap.remove(column.name, column)) column.parent = null;
        revalidate();
    }

    public void revalidate() {
        this.sorted = attributeMap.values().stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < this.sorted.size(); i++) this.sorted.get(i).index = i;
        keyCount = getKeys().size();
        colCount = getCols().size();
    }

    private static final Font title = new Font(null, Font.BOLD, 12);
    private static final Font normal = new Font(null, Font.PLAIN, 12);
    private static final Font small = new Font(null, Font.PLAIN, 10);

    @Override
    public void draw(@NotNull DiagramGraphics g) {
        AffineTransform transform = g.getTransform();
        g.translate(getX(), getY());

        g.setFont(title);
        g.drawStringCenter(name, (float) (Column.WIDTH / 2), (float) (Column.HEIGHT / 2 + 10));
        g.setFont(normal);

        if (highlighted) g.draw(getShape());
        g.translate(0, Column.HEIGHT);
        for (Column e : sorted) {
            e.draw(g);
            g.translate(Column.WIDTH, 0);
        }
        g.setTransform(transform);

        Vector offset = this.add(sorted.size() * Column.WIDTH, Column.HEIGHT);
        for (Tuple<Boolean, String, Table> other : foreign) {
            other.getC().drawAsForeign(g, other.getA(), other.getB(), offset.clone());
            offset.incre(other.getC().keyCount * Column.WIDTH, 0);
        }
        if (highlighted) g.draw(getShapeWorld(), new Color(0, 0, 0, 30), Color.BLACK);
    }

    @Override
    public void predraw(DiagramGraphics g) {
        Vector offset = this.add(sorted.size() * Column.WIDTH, Column.HEIGHT);
        for (Tuple<Boolean, String, Table> other : foreign) {
            other.getC().predrawAsForeign(g, offset.clone(), other.getC());
            offset.incre(other.getC().keyCount * Column.WIDTH, 0);
        }
    }

    /**
     * Draw this table's keys onto position specified
     *
     * @param g      graphics
     * @param isKey  if everything is key
     * @param role   role this table plays
     * @param origin to draw
     */
    public void drawAsForeign(@NotNull DiagramGraphics g, Boolean isKey, String role, @NotNull Vector origin) {
        double diff = keyCount * .5 * Column.WIDTH;
        Vector center = origin.add(diff, 7);
        for (Column key : keys) {
            key.drawAsForeign(g, isKey, origin);
            origin.incre(Column.WIDTH, 0);
        }
        if (((RSDiagram) g.getContext()).showBrackets.get()) {
            g.draw(new RangeLine(center.add(-diff, 0), center.add(diff, 0)));
            Font font = g.getFont();
            g.setFont(small);
            g.drawStringCenter(role, center, Color.WHITE);
            g.setFont(font);
        }
    }

    public void predrawAsForeign(DiagramGraphics g, Vector origin, Table parent) {
        for (Column key : keys) {
            key.predrawAsForeign(g, origin, parent);
            origin.incre(Column.WIDTH, 0);
        }
    }

    public Stream<Column> selfKeys() {
        return sorted.stream().filter(e -> e.key);
    }

    /**
     * Combines keys and all required foreign keys
     *
     * @return minimal set of keys
     */
    public List<Column> getKeys() {
        return keys = Stream.concat(selfKeys(),
                foreign.stream().filter(Tuple::getA).flatMap(e -> e.getC().getKeys().stream())).collect(Collectors.toList());
    }

    /**
     * Combine all columns and all foreign keys
     *
     * @return all columns
     */
    public List<Column> getCols() {
        return cols = Stream.concat(sorted.stream(), foreign.stream().flatMap(e -> e.getC().getKeys().stream())).collect(Collectors.toList());
    }

    public Vector positionOf(Column column) {
        return this.add(cols.indexOf(column) * Column.WIDTH, Column.HEIGHT);
    }

    public Rectangle2D getShapeWorld() {
        return new Rectangle2D.Double(getX(), getY(), colCount * Column.WIDTH, Column.HEIGHT * 2);
    }

    public Rectangle2D getShape() {
        return new Rectangle2D.Double(0, 0, colCount * Column.WIDTH, Column.HEIGHT * 2);
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
}
