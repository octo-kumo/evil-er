package shapes.lines;

import model.Drawable;
import model.er.Node;

public abstract class Line<A extends Node, B extends Node> implements Drawable {
    public enum LineStyle {
        STRAIGHT, AXIS_ALIGNED, CURVE
    }

    public A a;
    public B b;

    public Line(A a, B b) {
        this.a = a;
        this.b = b;
    }
}
