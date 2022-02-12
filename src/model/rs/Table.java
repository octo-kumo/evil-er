package model.rs;

import main.renderer.DiagramGraphics;
import main.rs.RSDiagram;
import model.Drawable;
import model.Vector;
import org.jetbrains.annotations.NotNull;
import shapes.lines.RangeLine;
import utils.models.Tuple;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Table extends Vector implements Drawable {
    private static final Font title = new Font(null, Font.BOLD, 12);
    private static final Font normal = new Font(null, Font.PLAIN, 12);
    private static final Font small = new Font(null, Font.PLAIN, 10);
    public final List<Tuple<Boolean, String, Table>> foreign;
    public final Map<String, Column> attributeMap;
    private String name;
    public long keyCount = 0;
    public long colCount = 0;
    private List<Column> sorted;
    private boolean highlighted;
    private List<Column> keys;
    private List<Column> cols;

    public Table(String name) {
        this.name = name;
        this.attributeMap = new HashMap<>();
        this.sorted = new ArrayList<>();
        this.foreign = new ArrayList<>();
    }

    public boolean add(@NotNull Column column) {
        column.setParent(this);
        boolean n = this.attributeMap.put(column.getName(), column) == null;
        revalidate();
        return n;
    }

    public void add(Table other, String name, boolean required) {
        this.foreign.add(new Tuple<>(required, name, other));
        revalidate();
    }


    public boolean remove(Column column) {
        if (this.attributeMap.remove(column.getName(), column)) {
            column.setParent(null);
            revalidate();
            return true;
        }
        return false;
    }

    public void revalidate() {
        this.sorted = attributeMap.values().stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < this.sorted.size(); i++) this.sorted.get(i).setIndex(i);
        keyCount = getKeys().size();
        colCount = getCols().size();
    }

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
            other.c.drawAsForeign(g, other.a, other.b, offset.clone());
            offset.incre(other.c.keyCount * Column.WIDTH, 0);
        }
        if (highlighted) {
            g.setColor(g.context.highlight());
            g.draw(getShapeWorld());
            g.setColor(g.context.foreground());
        }
    }

    @Override
    public void predraw(DiagramGraphics g) {
        Vector offset = this.add(sorted.size() * Column.WIDTH, Column.HEIGHT);
        for (Tuple<Boolean, String, Table> other : foreign) {
            other.c.predrawAsForeign(g, offset.clone(), other.c);
            offset.incre(other.c.keyCount * Column.WIDTH, 0);
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
            g.drawStringCenter(role, center, g.context.background());
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
        return sorted.stream().filter(Column::isKey);
    }

    /**
     * Combines keys and all required foreign keys
     *
     * @return minimal set of keys
     */
    public List<Column> getKeys() {
        return keys = Stream.concat(selfKeys(),
                foreign.stream().filter(f -> f.a).flatMap(e -> e.c.getKeys().stream())).collect(Collectors.toList());
    }

    /**
     * Combine all columns and all foreign keys
     *
     * @return all columns
     */
    public List<Column> getCols() {
        return cols = Stream.concat(sorted.stream(), foreign.stream().flatMap(e -> e.c.getKeys().stream())).collect(Collectors.toList());
    }

    public Vector positionOf(Column column) {
        return this.add(cols.indexOf(column) * Column.WIDTH, Column.HEIGHT);
    }

    public Shape getShapeWorld() {
        AffineTransform tx = new AffineTransform();
        tx.translate(getX(), getY());
        return tx.createTransformedShape(getShape());
    }

    public Rectangle2D getShape() {
        return new Rectangle2D.Double(0, 0, Math.max(colCount, 1) * Column.WIDTH, Column.HEIGHT * 2);
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
