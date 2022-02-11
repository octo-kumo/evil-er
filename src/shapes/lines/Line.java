package shapes.lines;

import model.Drawable;
import model.Vector;
import model.er.Node;

public abstract class Line<A extends Node, B extends Node> implements Drawable {

    public A a;
    public B b;
    public Line(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public abstract Vector getStart();

    public abstract Vector getEnd();

    public enum LineStyle {
        STRAIGHT, AXIS_ALIGNED, CURVE
    }
}
