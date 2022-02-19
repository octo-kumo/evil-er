package utils.callbacks;

import shapes.lines.Line;

import java.awt.*;

public interface DrawContext {
    Line.LineStyle getLineStyle();

    default Color foreground() {
        return Color.BLACK;
    }

    default Color background() {
        return Color.WHITE;
    }

    default Color fill() {
        return Color.WHITE;
    }

    default Color highlight() {
        return new Color(230, 230, 230);
    }

    default Color disabled() {
        return new Color(fill().getRGB() | 0xaa000000, true);
    }

    default boolean drawDebugInfo() {
        return false;
    }
}
