package shapes.lines;

import main.rs.RSDiagram;
import model.Range;
import model.Vector;
import model.callbacks.DrawContext;
import model.er.Attribute;
import model.others.Pair;
import model.others.Tuple;
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
    public static ArrayList<Tuple<Integer, Integer, Integer>> xTaken = new ArrayList<>();
    public static ArrayList<Tuple<Integer, Integer, Integer>> yTaken = new ArrayList<>();
    public static ArrayList<Pair<Line2D, Point2D>> jumped = new ArrayList<>();
    public static ArrayList<Line2D.Double> toDodge = new ArrayList<>();

    public static int getTarget(ArrayList<Tuple<Integer, Integer, Integer>> axis, int srcI, int from, int to, int diff, boolean flip) {
        final AtomicInteger src = new AtomicInteger(srcI);
        final int gap = diff;
        while (axis.parallelStream().anyMatch(e -> e.a == src.get() && Range.intersects(e.b, e.c, from, to))) {
            src.set(srcI + diff);
            if (flip) diff = diff > 0 ? -diff : -diff + gap;
            else diff += gap;
        }
        axis.add(new Tuple<>(src.get(), from, to));
        return src.get();
    }

    public static void resetLines() {
        xTaken.clear();
        yTaken.clear();
        toDodge.clear();
    }

    public SchemaLine(Vector a, Vector b, DrawContext context) {
        if (context.getLineStyle() == Line.LineStyle.AXIS_ALIGNED) axisLine(a, b, context);
        else if (context.getLineStyle() == Line.LineStyle.STRAIGHT) straightLine(a, b, context);
        else axisCurvyLine(a, b, context);
    }

    public void straightLine(@NotNull Vector a, @NotNull Vector b, DrawContext context) {
        boolean bUp = getBUp(a, b);
        moveTo(a.getX(), a.getY());
        lineTo(a.getX(), a.getY() + (getAUp(a, b) ? -30 : 30));
        dodgingTo(b.getX(), b.getY() + (bUp ? -30 : 30), context);
        lineTo(b.getX(), b.getY());
        arrow(bUp, b.getX(), b.getY() + (bUp ? -Column.HEIGHT / 2 : Column.HEIGHT / 2));
    }

    public void axisLine(@NotNull Vector a, @NotNull Vector b, DrawContext context) {
        boolean aUp = getAUp(a, b);
        boolean bUp = getBUp(a, b);
        int aY = (int) (a.getY() + (aUp ? -30 : 30));
        int bY = (int) (b.getY() + (bUp ? -30 : 30));
        int targetX = (int) b.getX();
        int targetY = aUp && bUp ? Math.min(aY, bY) : aUp ? aY : bUp ? bY : Math.max(aY, bY);
        if (((RSDiagram) context).avoidOverlap.get()) {
            targetX = getTarget(xTaken, targetX, (int) a.getY(), (int) b.getY(), 10, true);
            targetY = getTarget(yTaken, targetY, (int) b.getX(), (int) a.getX(), aUp || bUp ? -7 : 7, false);
        }

        moveTo(a.getX(), a.getY());
        dodgingTo(a.getX(), targetY, context);
        dodgingTo(targetX, targetY, context);
        dodgingTo(targetX, b.getY(), context);
        arrow(bUp, targetX, b.getY() + (bUp ? -Column.HEIGHT / 2 : Column.HEIGHT / 2));
    }

    private static final double RADIUS = 5;

    public void axisCurvyLine(@NotNull Vector a, @NotNull Vector b, DrawContext context) {
        if (Math.abs(a.getX() - b.getX()) < RADIUS * 2) {
            axisLine(a, b, context);
            return;
        }

        boolean aUp = getAUp(a, b);
        boolean bUp = getBUp(a, b);
        int aY = (int) (a.getY() + (aUp ? -30 : 30));
        int bY = (int) (b.getY() + (bUp ? -30 : 30));
        int targetX = (int) b.getX();
        int targetY = aUp && bUp ? Math.min(aY, bY) : aUp ? aY : bUp ? bY : Math.max(aY, bY);
        if (((RSDiagram) context).avoidOverlap.get()) {
            targetX = getTarget(xTaken, targetX, (int) a.getY(), (int) b.getY(), 10, true);
            targetY = getTarget(yTaken, targetY, (int) b.getX(), (int) a.getX(), aUp || bUp ? -7 : 7, false);
        }

        moveTo(a.getX(), a.getY());

        dodgingTo(a.getX(), targetY + (aUp ? RADIUS : -RADIUS), context);
        quadTo(a.getX(), targetY, a.getX() + (b.getX() > a.getX() ? RADIUS : -RADIUS), targetY);

        dodgingTo(targetX + (b.getX() > a.getX() ? -RADIUS : RADIUS), targetY, context);
        quadTo(targetX, targetY, targetX, targetY + (bUp ? RADIUS : -RADIUS));

        dodgingTo(targetX, b.getY(), context);
        arrow(bUp, targetX, b.getY() + (bUp ? -Column.HEIGHT / 2 : Column.HEIGHT / 2));
    }

    private boolean getAUp(Vector a, Vector b) {
        if (Math.abs(b.getY() - a.getY()) < (Attribute.HEIGHT + 20)) return false;
        else return b.getY() < a.getY();
    }

    private boolean getBUp(Vector a, Vector b) {
        if (Math.abs(b.getY() - a.getY()) < (Attribute.HEIGHT + 20)) return false;
        return b.getY() > a.getY();
    }

    private static final double JUMP_LINE_RADIUS = 5;

    public void dodgingTo(@NotNull Vector vector, DrawContext context) {
        if (!((RSDiagram) context).jumpLines.get()) {
            lineTo(vector);
            return;
        }
        Vector currentPoint = new Vector(getCurrentPoint());
        Vector diff = vector.minus(currentPoint);
        double len = diff.len();
        Vector dir = diff.norm().multi(JUMP_LINE_RADIUS);
        Vector r90 = dir.rotate90();
        Line2D.Double line = new Line2D.Double(currentPoint, vector);
        toDodge.parallelStream()
                .map(l -> {
                    Vector point = intersection(l, line);
                    return jumped.parallelStream().anyMatch(j -> Objects.equals(point, j.b) && j.a == l) ? null : point;
                })
                .filter(Objects::nonNull)
                .distinct()
                .map(p -> new Pair<>(p, currentPoint.minus(p).len()))
                .filter(p -> !(p.b < JUMP_LINE_RADIUS || len - p.b < JUMP_LINE_RADIUS))
                .sorted(Comparator.comparing(a -> a.b))
                .forEachOrdered(pair -> {
                    jumped.add(new Pair<>(line, pair.a));

                    Vector p = pair.a;
                    lineTo(p.minus(dir));
                    curveTo(p.minus(dir).add(r90),
                            p.add(dir).add(r90),
                            p.add(dir));
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

    public void dodgingTo(double x, double y, DrawContext context) {
        dodgingTo(new Vector(x, y), context);
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
        double x0 = a.getX1(), y0 = a.getY1(),
                x1 = a.getX2(), y1 = a.getY2(),
                x2 = b.getX1(), y2 = b.getY1(),
                x3 = b.getX2(), y3 = b.getY2();
        double sx1 = x1 - x0,
                sy1 = y1 - y0,
                sx2 = x3 - x2,
                sy2 = y3 - y2;
        double v = -sx2 * sy1 + sx1 * sy2;
        if (v == 0) return null;
        double s = (-sy1 * (x0 - x2) + sx1 * (y0 - y2)) / v;
        double t = (sx2 * (y0 - y2) - sy2 * (x0 - x2)) / v;
        if (s > 0 && s < 1 && t > 0 && t < 1) return new Vector(x0 + (t * sx1), y0 + (t * sy1));
        return null;
    }
}
