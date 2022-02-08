package shapes.lines;

import model.Range;
import model.Vector;
import model.er.Attribute;
import model.others.Pair;
import model.rs.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class SchemaLine extends Path2D.Double {
    public static ArrayList<Pair<Integer, Range>> xTaken = new ArrayList<>();
    public static ArrayList<Pair<Integer, Range>> yTaken = new ArrayList<>();

    public static ArrayList<Line2D.Double> toDodge = new ArrayList<>();

    public static int getTarget(ArrayList<Pair<Integer, Range>> axis, int srcI, Range range, int diff) {
        final AtomicInteger src = new AtomicInteger(srcI);
        while (axis.stream().anyMatch(e -> e.getA() == src.get() && e.getB().intersects(range))) {
            src.getAndAdd(diff);
//            if (diff > 0) diff = -diff;
//            else diff = -diff + 5;
        }
        axis.add(new Pair<>(src.get(), range));
        return src.get();
    }

    public static void resetLines() {
        xTaken.clear();
        yTaken.clear();
        toDodge.clear();
    }

    public SchemaLine(Vector a, Vector b, Line.LineStyle style) {
        if (style == Line.LineStyle.AXIS_ALIGNED) axisLine(a, b);
        else if (style == Line.LineStyle.STRAIGHT) straightLine(a, b);
        else axisCurvyLine(a, b);
    }

    public void straightLine(@NotNull Vector a, @NotNull Vector b) {
        int state = getState(a, b);
        boolean aUp = getAUp(state, a, b);
        boolean bUp = getBUp(state, a, b);
        moveTo(a.getX(), a.getY());
        lineTo(a.getX(), a.getY() + (aUp ? -30 : 30));
        dodgingTo(b.getX(), b.getY() + (bUp ? -30 : 30));
        lineTo(b.getX(), b.getY());
        arrow(bUp, b.getX(), b.getY() + (bUp ? -Column.HEIGHT / 2 : Column.HEIGHT / 2));
    }

    public void axisLine(@NotNull Vector a, @NotNull Vector b) {
        int state = getState(a, b);
        boolean aUp = getAUp(state, a, b);
        boolean bUp = getBUp(state, a, b);
        int aY = (int) (a.getY() + (aUp ? -30 : 30));
        int bY = (int) (b.getY() + (bUp ? -30 : 30));
        int targetX = getTarget(xTaken, (int) b.getX(), new Range(a.getY(), b.getY()), 10);
        int targetY = getTarget(yTaken,
                aUp && bUp ? Math.min(aY, bY) : aUp ? aY : bUp ? bY : Math.max(aY, bY),
                new Range(b.getX(), a.getX()),
                aUp || bUp ? -5 : 5);

        moveTo(a.getX(), a.getY());
        dodgingTo(a.getX(), targetY);
        dodgingTo(targetX, targetY);
        dodgingTo(targetX, b.getY());
        arrow(bUp, targetX, b.getY() + (bUp ? -Column.HEIGHT / 2 : Column.HEIGHT / 2));
    }

    private static final double RADIUS = 5;

    public void axisCurvyLine(@NotNull Vector a, @NotNull Vector b) {
        if (Math.abs(a.getX() - b.getX()) < RADIUS * 2) {
            axisLine(a, b);
            return;
        }
        int state = getState(a, b);

        boolean aUp = getAUp(state, a, b);
        boolean bUp = getBUp(state, a, b);
        int aY = (int) (a.getY() + (aUp ? -30 : 30));
        int bY = (int) (b.getY() + (bUp ? -30 : 30));
        int targetX = getTarget(xTaken, (int) b.getX(), new Range(a.getY(), b.getY()), 10);
        int targetY = getTarget(yTaken,
                aUp && bUp ? Math.min(aY, bY) : aUp ? aY : bUp ? bY : Math.max(aY, bY),
                new Range(b.getX(), a.getX()),
                aUp || bUp ? -5 : 5);

        moveTo(a.getX(), a.getY());

        dodgingTo(a.getX(), targetY + (aUp ? RADIUS : -RADIUS));
        quadTo(a.getX(), targetY, a.getX() + (b.getX() > a.getX() ? RADIUS : -RADIUS), targetY);

        dodgingTo(targetX + (b.getX() > a.getX() ? -RADIUS : RADIUS), targetY);
        quadTo(targetX, targetY, targetX, targetY + (bUp ? RADIUS : -RADIUS));

        dodgingTo(targetX, b.getY());
        arrow(bUp, targetX, b.getY() + (bUp ? -Column.HEIGHT / 2 : Column.HEIGHT / 2));
    }

    /**
     * @param a point a
     * @param b point b
     * @return - 1 if b below a (should a down, b up)
     * - -1 if a below b (should a up, b down)
     * - 0 if they should have same state
     */
    private int getState(Vector a, Vector b) {
        return Math.abs(b.getY() - a.getY()) < (Attribute.HEIGHT + 20) ? 0 :
                b.getY() > a.getY() ? 1 : -1;
    }

    private boolean getAUp(int state, Vector a, Vector b) {
        if (state < 0) return true;
        else if (state > 0) return false;
        else return true;
    }

    private boolean getBUp(int state, Vector a, Vector b) {
        if (state < 0) return false;
        else if (state > 0) return true;
        else return true;
    }

    public void dodgingTo(@NotNull Vector vector) {
        Vector currentPoint = new Vector(getCurrentPoint());
        Vector diff = vector.minus(currentPoint);
        Vector dir = diff.norm().multi(5);
        Vector r90 = dir.rotate90();
        Line2D.Double line = new Line2D.Double(currentPoint, vector);
        toDodge.stream()
                .filter(l -> noEndsMeet(l, line))
                .filter(l -> l.intersectsLine(line))
                .map(l -> intersection(l, line))
                .filter(Objects::nonNull)
                .distinct()
                .map(p -> new Pair<>(p, currentPoint.minus(p).len()))
                .sorted(Comparator.comparingDouble(Pair::getB))
                .forEach(pair -> {
                    Vector p = pair.getA();
//                    lineTo(p.minus(dir));
//                    curveTo(p.minus(dir).add(r90),
//                            p.add(dir).add(r90),
//                            p.add(dir));
                    lineTo(p.minus(dir));
                    lineTo(p.add(dir).add(r90));
                    moveTo(p.minus(dir).minus(r90));
                    lineTo(p.add(dir));
                });
        lineTo(vector.getX(), vector.getY());
        toDodge.add(line);
    }

    private boolean noEndsMeet(Line2D.@NotNull Double l, Line2D.@NotNull Double line) {
        return !(
                Objects.equals(l.getP1(), line.getP1()) ||
                        Objects.equals(l.getP2(), line.getP1()) ||
                        Objects.equals(l.getP1(), line.getP2()) ||
                        Objects.equals(l.getP2(), line.getP2())
        );
    }

    public void dodgingTo(double x, double y) {
        dodgingTo(new Vector(x, y));
    }


    public void arrow(boolean up, double x, double y) {
        moveTo(x, y);
        lineTo(x - 5, y + (up ? -5 : 5));
        moveTo(x, y);
        lineTo(x + 5, y + (up ? -5 : 5));
        moveTo(x, y);
    }

    public void lineTo(@NotNull Point2D point) {
        lineTo(point.getX(), point.getY());
    }

    public void moveTo(@NotNull Point2D point) {
        moveTo(point.getX(), point.getY());
    }

    public void curveTo(@NotNull Point2D a, @NotNull Point2D b, @NotNull Point2D c) {
        curveTo(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY());
    }

    public void arrow(boolean up, @NotNull Vector pos) {
        arrow(up, pos.getX(), pos.getY());
    }

    public static @Nullable Vector intersection(@NotNull Line2D a, @NotNull Line2D b) {
        double x1 = a.getX1(), y1 = a.getY1(), x2 = a.getX2(), y2 = a.getY2(), x3 = b.getX1(), y3 = b.getY1(),
                x4 = b.getX2(), y4 = b.getY2();
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0) return null;

        double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

        return new Vector(xi, yi);
    }
}
