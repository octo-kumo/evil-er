package shapes.lines;

import model.Vector;
import model.rs.Attribute;

import java.awt.geom.Path2D;

public class SchemaLine extends Path2D.Double {
    public SchemaLine(Vector a, Vector b) {
        int state = Math.abs(b.getY() - a.getY()) < 30 ? 0 : b.getY() > a.getY() ? 1 : -1;
        moveTo(a.getX(), a.getY());
        lineTo(a.getX(), a.getY() + (state >= 0 ? 30 : -30));
        lineTo(b.getX(), b.getY() - (state > 0 ? 30 : -30));
        lineTo(b.getX(), b.getY());
        arrow(state <= 0, b.getX(), b.getY() - (state > 0 ? Attribute.HEIGHT / 2 : -Attribute.HEIGHT / 2));
    }

    public void arrow(boolean up, double x, double y) {
        moveTo(x, y);
        lineTo(x - 5, y + (up ? 5 : -5));
        moveTo(x, y);
        lineTo(x + 5, y + (up ? 5 : -5));
        moveTo(x, y);
    }

    public void arrow(boolean up, Vector pos) {
        arrow(up, pos.getX(), pos.getY());
    }
}
