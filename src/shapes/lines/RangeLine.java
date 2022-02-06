package shapes.lines;

import model.Vector;

import java.awt.geom.Path2D;

public class RangeLine extends Path2D.Double {
    public RangeLine(Vector a, Vector b) {
        moveTo(a.getX() + 4, a.getY() + 5);
        lineTo(a.getX() + 4, a.getY());
        lineTo(b.getX() - 4, b.getY());
        lineTo(b.getX() - 4, b.getY() + 5);
    }
}
