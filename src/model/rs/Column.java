package model.rs;

import main.renderer.DiagramGraphics;
import model.Drawable;
import model.ImmutableVector;
import model.Vector;
import org.jetbrains.annotations.NotNull;
import shapes.lines.SchemaLine;

import java.awt.geom.Rectangle2D;

public class Column extends Vector implements Drawable, Comparable<Column> {
    public static final double WIDTH = 100;
    public static final double HEIGHT = 40;
    public static final ImmutableVector OFFSET = new ImmutableVector() {{
        x = WIDTH / 2;
        y = HEIGHT / 2;
    }};

    private int index;
    private Table parent;
    private String name;
    private boolean key;

    public Column(String name, boolean key) {
        this.name = name;
        this.key = key;
    }

    public double getX() {
        return parent.getX() + WIDTH * index;
    }

    public double getY() {
        return parent.getY() + HEIGHT;
    }

    @Override
    public void draw(@NotNull DiagramGraphics g) {
        g.draw(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT), g.context.fill(), g.context.foreground());
        g.drawStringCenter(name, (float) (WIDTH / 2), (float) (HEIGHT / 2));
        if (key) g.draw(g.lineUnderString(name, (float) (WIDTH / 2), (float) (HEIGHT / 2 + 3)));
    }


    @Override
    public int compareTo(@NotNull Column other) {
        return Boolean.compare(other.key, this.key);
    }

    public void predrawAsForeign(@NotNull DiagramGraphics g, @NotNull Vector origin, Table parent) {
        g.draw(new SchemaLine(origin.add(OFFSET), parent.positionOf(this).add(OFFSET), g.getContext()));
    }

    public void drawAsForeign(@NotNull DiagramGraphics g, @NotNull Boolean key, @NotNull Vector origin) {
        g.draw(new Rectangle2D.Double(origin.getX(), origin.getY(), WIDTH, HEIGHT), g.context.fill(), g.context.foreground());
        g.drawStringCenter(name + " (FK)", origin.add(WIDTH / 2, HEIGHT / 2));
        if (key) g.draw(g.lineUnderString(name + " (FK)", origin.add(WIDTH / 2, HEIGHT / 2 + 3)));
    }

    @Override
    public String toString() {
        return "Column{" +
                "index=" + index +
                ", parent=" + parent +
                ", name='" + name + '\'' +
                ", key=" + key +
                '}';
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Table getParent() {
        return parent;
    }

    public void setParent(Table parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }
}
