package shapes.lines;

import model.Vector;
import model.rs.Column;

import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Objects;

public class SchemaLine extends Path2D.Double {
    public static HashMap<Integer, Vector> xTaken = new HashMap<>();
    public static HashMap<Integer, Vector> yTaken = new HashMap<>();

    public static void resetLines() {
        xTaken.clear();
        yTaken.clear();
    }

    public SchemaLine(Vector a, Vector b, Line.LineStyle style) {
        if (style == Line.LineStyle.AXIS_ALIGNED) axisLine(a, b);
        else straightLine(a, b);
    }

    public void straightLine(Vector a, Vector b) {
        int state = Math.abs(b.getY() - a.getY()) < 30 ? 0 : b.getY() > a.getY() ? 1 : -1;
        moveTo(a.getX(), a.getY());
        lineTo(a.getX(), a.getY() + (state >= 0 ? 30 : -30));
        lineTo(b.getX(), b.getY() - (state > 0 ? 30 : -30));
        lineTo(b.getX(), b.getY());
        arrow(state <= 0, b.getX(), b.getY() - (state > 0 ? Column.HEIGHT / 2 : -Column.HEIGHT / 2));
    }

    public void axisLine(Vector a, Vector b) {
        int state = Math.abs(b.getY() - a.getY()) < 30 ? 0 : b.getY() > a.getY() ? 1 : -1;

        int targetX = (int) b.getX();
        while (xTaken.get(targetX) != null && !Objects.equals(xTaken.get(targetX), b)) targetX += 10;
        xTaken.put(targetX, b);

        int targetY = (int) (a.getY() + (state >= 0 ? 30 : -30));
        while (yTaken.get(targetY) != null && !Objects.equals(yTaken.get(targetY), a)) targetY += state >= 0 ? 10 : -10;
        yTaken.put(targetY, a);

        moveTo(a.getX(), a.getY());
        lineTo(a.getX(), targetY);
        lineTo(targetX, targetY);
        lineTo(targetX, b.getY());
        arrow(state <= 0, targetX, b.getY() - (state > 0 ? Column.HEIGHT / 2 : -Column.HEIGHT / 2));
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
