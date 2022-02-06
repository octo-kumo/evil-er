package shapes.lines;

import model.Vector;

import java.awt.geom.Path2D;

public class FancyLine extends Path2D.Double {
    public FancyLine(Vector a, Vector b, Line.LineStyle style) {
        Vector d = b.minus(a);
        Vector s = a.add(b);
        boolean horizontal = Math.abs(d.getY()) < Math.abs(d.getX());
        double mid = horizontal ? s.getX() / 2 : s.getY() / 2;
        switch (style) {
            case STRAIGHT:
                moveTo(a.getX(), a.getY());
                lineTo(b.getX(), b.getY());
                break;
            case AXIS_ALIGNED:
                moveTo(a.getX(), a.getY());
                if (horizontal) {
                    lineTo(mid, a.getY());
                    lineTo(mid, b.getY());
                } else {
                    lineTo(a.getX(), mid);
                    lineTo(b.getX(), mid);
                }
                lineTo(b.getX(), b.getY());
                break;
            case CURVE:
                moveTo(a.getX(), a.getY());
                if (horizontal) curveTo(mid, a.getY(), mid, b.getY(), b.getX(), b.getY());
                else curveTo(a.getX(), mid, b.getX(), mid, b.getX(), b.getY());
                break;
        }
    }
}
