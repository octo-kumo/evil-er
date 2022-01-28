package shapes;

import model.lines.Line;

import java.awt.geom.Path2D;

public class FancyLine extends Path2D.Double {
    public FancyLine(double x1, double y1, double x2, double y2, Line.LineStyle style) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        boolean horizontal = Math.abs(dy) < Math.abs(dx);
        double mid = horizontal ? (x1 + x2) / 2 : (y1 + y2) / 2;
        switch (style) {
            case STRAIGHT -> {
                moveTo(x1, y1);
                lineTo(x2, y2);
            }
            case AXIS_ALIGNED -> {
                moveTo(x1, y1);
                if (horizontal) {
                    lineTo(mid, y1);
                    lineTo(mid, y2);
                } else {
                    lineTo(x1, mid);
                    lineTo(x2, mid);
                }
                lineTo(x2, y2);
            }
            case CURVE -> {
                moveTo(x1, y1);
                if (horizontal) curveTo(mid, y1, mid, y2, x2, y2);
                else curveTo(x1, mid, x2, mid, x2, y2);
            }
        }
    }
}
