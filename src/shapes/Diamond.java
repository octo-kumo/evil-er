package shapes;

import java.awt.geom.Path2D;

public class Diamond extends Path2D.Double {
    public Diamond(double x, double y, double width, double height) {
        moveTo(x, y + height / 2);
        lineTo(x + width / 2, y);
        lineTo(x + width, y + height / 2);
        lineTo(x + width / 2, y + height);
        closePath();
    }
}