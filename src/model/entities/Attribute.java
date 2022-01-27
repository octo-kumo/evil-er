package model.entities;

import model.Node;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Attribute extends Entity {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 40;

    public Entity parent;

    public Attribute setParent(Entity parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public void prepaint(Graphics2D g) {
        super.prepaint(g);
        g.drawLine(0, 0, -x, -y);
    }

    @Override
    public void drawShape(Graphics2D g) {
        Node.draw(g, new Ellipse2D.Double(-WIDTH / 2d, -HEIGHT / 2d, WIDTH, HEIGHT));
        if (weak) Node.draw(g, new Ellipse2D.Double(-INNER_WIDTH / 2d, -INNER_HEIGHT / 2d, INNER_WIDTH, INNER_HEIGHT));
    }
}
