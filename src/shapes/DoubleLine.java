package shapes;

import java.awt.geom.Path2D;

public class DoubleLine extends Path2D.Double {
    public DoubleLine(double x1, double y1, double x2, double y2) {
        this(x1, y1, x2, y2, 2.5);
    }

    public DoubleLine(double x1, double y1, double x2, double y2, double spacing) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double offsetx = Math.sin(angle) * spacing, offsety = Math.cos(angle) * spacing;
        moveTo(x1 + offsetx, y1 + offsety);
        lineTo(x2 + offsetx, y2 + offsety);
        lineTo(x2 - offsetx, y2 - offsety);
        lineTo(x1 - offsetx, y1 - offsety);
        closePath();
    }
}