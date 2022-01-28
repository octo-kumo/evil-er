package model;

import main.renderer.DiagramGraphics;

public interface Drawable {
    default void predraw(DiagramGraphics g) {
    }

    void draw(DiagramGraphics g);
}
