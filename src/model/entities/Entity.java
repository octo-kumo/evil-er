package model.entities;

import model.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class Entity extends Node {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 40;
    public static final int INNER_WIDTH = WIDTH - 6;
    public static final int INNER_HEIGHT = HEIGHT - 6;
    public String name;
    public boolean weak;

    public ArrayList<Attribute> attributes;

    public Entity() {
        attributes = new ArrayList<>();
    }

    public Entity addAttribute(Attribute attribute) {
        this.attributes.add(attribute.setParent(this));
        return this;
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
    public void prepaint(Graphics2D g) {
        attributes.forEach(a -> a.predraw(g));
    }

    @Override
    public void paint(Graphics2D g) {
        attributes.forEach(a -> a.draw(g));
        drawShape(g);
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int cx = -metrics.stringWidth(name) / 2;
        int cy = -metrics.getHeight() / 2 + metrics.getAscent();
        g.drawString(name, cx, cy);
    }

    public void drawShape(Graphics2D g) {
        Node.draw(g, new Rectangle2D.Double(-WIDTH / 2d, -HEIGHT / 2d, WIDTH, HEIGHT));
        if (weak)
            Node.draw(g, new Rectangle2D.Double(-INNER_WIDTH / 2d, -INNER_HEIGHT / 2d, INNER_WIDTH, INNER_HEIGHT));
    }
}
