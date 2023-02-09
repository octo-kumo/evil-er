package shapes;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class CompositeShape implements Shape {
    private final Shape[] shapes;

    public CompositeShape(Shape... shapes) {
        this.shapes = shapes;
        Rectangle2D bounds = null;
        for (Shape s : shapes) bounds = bounds == null ? s.getBounds2D() : s.getBounds2D().createUnion(bounds);
    }

    @Override
    public Rectangle getBounds() {
        return null;
    }

    @Override
    public Rectangle2D getBounds2D() {
        return null;
    }

    @Override
    public boolean contains(double x, double y) {
        for (Shape s : shapes) if (s.contains(x, y)) return true;
        return false;
    }

    @Override
    public boolean contains(Point2D p) {
        for (Shape s : shapes) if (s.contains(p)) return true;
        return false;
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        for (Shape s : shapes) if (s.intersects(x, y, w, h)) return true;
        return false;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        for (Shape s : shapes) if (s.intersects(r)) return true;
        return false;
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        for (Shape s : shapes) if (s.contains(x, y, w, h)) return true;
        return false;
    }

    @Override
    public boolean contains(Rectangle2D r) {
        for (Shape s : shapes) if (s.contains(r)) return true;
        return false;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new CompositeIterator(at, 1, shapes);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new CompositeIterator(at, flatness, shapes);
    }

    public static class CompositeIterator implements PathIterator {

        private final PathIterator[] iterators;
        private int currentIterator = 0;

        public CompositeIterator(AffineTransform at, double flatness, Shape... shapes) {
            this.iterators = new PathIterator[shapes.length];
            for (int i = 0; i < shapes.length; i++) {
                iterators[i] = shapes[i].getPathIterator(at, flatness);
            }
        }

        @Override
        public int getWindingRule() {
            return iterators[currentIterator].getWindingRule();
        }

        @Override
        public boolean isDone() {
            return iterators.length - 1 == currentIterator && iterators[currentIterator].isDone();
        }

        @Override
        public void next() {
            if (iterators[currentIterator].isDone()) {
                if (currentIterator < iterators.length - 1) currentIterator++;
            } else iterators[currentIterator].next();
        }

        @Override
        public int currentSegment(float[] coords) {
            return iterators[currentIterator].currentSegment(coords);
        }

        @Override
        public int currentSegment(double[] coords) {
            return iterators[currentIterator].currentSegment(coords);
        }
    }
}
