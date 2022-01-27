package model.lines;

import model.Node;

import java.awt.*;

public class StraightLine<A extends Node, B extends Node> extends Line<A, B> {
    public StraightLine(A nodeA, B nodeB) {
        super(nodeA, nodeB);
    }

    @Override
    public void predraw(Graphics2D g) {
        g.drawLine(a.x, a.y, b.x, b.y);
    }

    @Override
    public void draw(Graphics2D g) {
        g.drawLine(a.x, a.y, b.x, b.y);
    }
}
