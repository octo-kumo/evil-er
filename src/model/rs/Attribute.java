package model.rs;

import main.renderer.DiagramGraphics;
import model.Drawable;
import model.ImmutableVector;
import model.Vector;
import org.jetbrains.annotations.NotNull;
import shapes.lines.SchemaLine;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Attribute extends Vector implements Drawable, Comparable<Attribute> {
    public static final double WIDTH = 100;
    public static final double HEIGHT = 40;
    public static final ImmutableVector OFFSET = new ImmutableVector() {{
        x = WIDTH / 2;
        y = HEIGHT / 2;
    }};

    public int index;
    public Table parent;
    public String name;
    public boolean key;

    public Attribute(String name, boolean key) {
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
    public void draw(DiagramGraphics g) {
        g.draw(new Rectangle2D.Double(0, 0, WIDTH, HEIGHT), Color.WHITE, Color.BLACK);
        g.drawStringCenter(name, (float) (WIDTH / 2), (float) (HEIGHT / 2));
        if (key) g.draw(g.lineUnderString(name, (float) (WIDTH / 2), (float) (HEIGHT / 2 + 3)));
    }


    @Override
    public int compareTo(@NotNull Attribute other) {
        return Boolean.compare(other.key, this.key);
    }

    public void drawLine(DiagramGraphics g, Vector origin) {
        g.draw(new SchemaLine(origin.add(OFFSET), this.add(OFFSET)));
    }

    public void drawAsForeign(DiagramGraphics g, Vector origin) {
        g.draw(new Rectangle2D.Double(origin.getX(), origin.getY(), WIDTH, HEIGHT), Color.WHITE, Color.BLACK);
        g.drawStringCenter(name + " (FK)", (float) (origin.getX() + WIDTH / 2), (float) (origin.getY() + HEIGHT / 2));
    }
}
