package model;

import java.awt.*;

public interface Drawable {
    default void predraw(Graphics2D g) {
    }

    void draw(Graphics2D g);
}
