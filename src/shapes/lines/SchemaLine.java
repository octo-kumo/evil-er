package shapes.lines;

import model.Vector;
import model.rs.Column;

import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.Objects;

public class SchemaLine extends Path2D.Double {
    public static HashMap<Integer, Vector> xTaken = new HashMap<>();
    public static HashMap<Integer, Vector> yTaken = new HashMap<>();

    public static int getTargetX(Vector vector) {
        int srcX = (int) vector.getX();
        while (xTaken.get(srcX) != null && !Objects.equals(xTaken.get(srcX), vector)) srcX += 10;
        xTaken.put(srcX, vector);
        return srcX;
    }

    public static int getTargetY(Vector vector, boolean up) {
        int srcY = (int) (vector.getY() + (up ? 30 : -30));
        while (yTaken.get(srcY) != null && !Objects.equals(yTaken.get(srcY), vector)) srcY += up ? 10 : -10;
        yTaken.put(srcY, vector);
        return srcY;
    }

    public static void resetLines() {
        xTaken.clear();
        yTaken.clear();
    }

    public SchemaLine(Vector a, Vector b, Line.LineStyle style) {
        if (style == Line.LineStyle.AXIS_ALIGNED) axisLine(a, b);
        else if (style == Line.LineStyle.STRAIGHT) straightLine(a, b);
        else axisCurvyLine(a, b);
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

        int targetX = getTargetX(b);
        int targetY = getTargetY(a, state >= 0);

        moveTo(a.getX(), a.getY());
        lineTo(a.getX(), targetY);
        lineTo(targetX, targetY);
        lineTo(targetX, b.getY());
        arrow(state <= 0, targetX, b.getY() - (state > 0 ? Column.HEIGHT / 2 : -Column.HEIGHT / 2));
    }

    private static final double RADIUS = 5;

    public void axisCurvyLine(Vector a, Vector b) {
        if (Math.abs(a.getX() - b.getX()) < RADIUS * 2) {
            axisLine(a, b);
            return;
        }
        int state = Math.abs(b.getY() - a.getY()) < 30 ? 0 : b.getY() > a.getY() ? 1 : -1;

        boolean aUp = state >= 0;
        boolean bUp = state <= 0;
        int targetX = getTargetX(b);
        int targetY = getTargetY(a, aUp);

        moveTo(a.getX(), a.getY());

        lineTo(a.getX(), targetY + (aUp ? -RADIUS : RADIUS));
        quadTo(a.getX(), targetY, a.getX() + (b.getX() > a.getX() ? RADIUS : -RADIUS), targetY);

        lineTo(targetX + (b.getX() > a.getX() ? -RADIUS : RADIUS), targetY);
        quadTo(targetX, targetY, targetX, targetY + (bUp ? -RADIUS : RADIUS));

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
