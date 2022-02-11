package main.rs;

import main.renderer.DiagramGraphics;
import model.Drawable;
import model.Vector;
import model.rs.Table;
import org.jetbrains.annotations.Nullable;
import shapes.lines.Line;
import shapes.lines.SchemaLine;
import utils.callbacks.DrawContext;
import utils.models.Reactive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RSDiagram extends JComponent implements MouseListener, MouseMotionListener, DrawContext, Drawable, MouseWheelListener {

    public static final Color FILL_DARK = new Color(0x222222);
    public static final Color BACKGROUND_DARK = new Color(0x121212);
    public static final Color FOREGROUND_DARK = new Color(0xefefef);
    public static double gridSize = 20;
    public static Color HIGHLIGHT = new Color(0xff0000); // dummy color
    public final ArrayList<Table> tables;
    public final Vector origin = new Vector();
    public final Reactive<Table> target = new Reactive<>();
    public final Reactive<Boolean> connecting = new Reactive<>(false);
    public final Reactive<Boolean> locked = new Reactive<>(false);
    public final Reactive<Boolean> aabb = new Reactive<>(false);
    public final Reactive<Boolean> grid = new Reactive<>(false);
    public final Reactive<Boolean> jumpLines = new Reactive<>(false);
    public final Reactive<Boolean> avoidOverlap = new Reactive<>(false);
    public final Reactive<Boolean> darkMode = new Reactive<>(false);
    public final Reactive<Boolean> showBrackets = new Reactive<>(false);
    private final Vector dragStart = new Vector(), targetStart = new Vector();
    public Reactive<Line.LineStyle> lineStyle = new Reactive<>(Line.LineStyle.CURVE);
    public double scale = 1;
    public double exportScale = 4;
    boolean exporting = false;
    /**
     * PARAMS
     */
    private Table adding_buf;
    private ActionType current = ActionType.Panning;

    public RSDiagram() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        tables = new ArrayList<>();
        addListeners();
        setPreferredSize(new Dimension(1280, 720));
    }

    private void addListeners() {
        lineStyle.addListener(s -> repaint());
        locked.addListener(s -> {
//            if (!s) setAddingType(Entity.Type.Select);
        });
    }

    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);
        DiagramGraphics g = new DiagramGraphics((Graphics2D) g1d, this);
        AffineTransform transform = g.getTransform();
        g.scale(scale, scale);
        g.translate(origin.getX(), origin.getY());

        if (grid.get()) {
            Vector lt = origin.negate();
            g.setColor(disabled());
            Vector sz = new Vector(getWidth(), getHeight()).div(scale);
            double maxX = lt.getX() + sz.getX();
            double maxY = lt.getY() + sz.getY();
            for (int x = (int) (Math.ceil(lt.getX() / 10) * 10); x < maxX; x += 10)
                g.drawLine(x, (int) lt.getY(), x, (int) maxY);
            for (int y = (int) (Math.ceil(lt.getY() / 10) * 10); y < maxY; y += 10)
                g.drawLine((int) lt.getX(), y, (int) maxX, y);
        }
        draw(g);
        g.setTransform(transform);
        g.drawStringCenter("@yun", getWidth() - 25, getHeight() - 15);
    }

    public void draw(DiagramGraphics g) {
        SchemaLine.resetLines();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(foreground());

        tables.forEach(d -> d.predraw(g));
        tables.forEach(d -> d.draw(g));

        if (exporting) return;
        g.setColor(disabled());
        if (aabb.get()) g.draw(getAABB());
    }

    public BufferedImage export() {
        setTarget(null);
        find(Table::isHighlighted).forEach(table -> table.setHighlighted(false));

        Rectangle2D aabb = getAABB();
        int padding = 20;
        aabb.add(aabb.getMinX() - padding, aabb.getMinY() - padding);
        aabb.add(aabb.getMaxX() + padding, aabb.getMaxY() + padding);
        aabb.setRect(aabb.getX() * exportScale, aabb.getY() * exportScale, aabb.getWidth() * exportScale, aabb.getHeight() * exportScale);
        BufferedImage img = new BufferedImage((int) aabb.getWidth(), (int) aabb.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(background());
        g.fillRect(0, 0, (int) aabb.getWidth(), (int) aabb.getHeight());
        g.translate(-aabb.getX(), -aabb.getY());
        g.scale(exportScale, exportScale);
        exporting = true;
        g.setFont(getFont());
        draw(new DiagramGraphics(g, this));
        exporting = false;
        g.dispose();
        return img;
    }

    public void delete(Table table) {
        if (target.get() == table) setTarget(null);
        else tables.remove(table);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (current == ActionType.Creating && adding_buf != null) {
            tables.add(adding_buf);
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            dragStart.set(e.getX(), e.getY());

            /* SELECT ELEMENT */
            /* IF NOT SELECT MULTIPLE, CLEAR HIGHLIGHT */
            if (target.get() != null && !e.isShiftDown()) {
                find(Table::isHighlighted).forEach(table -> table.setHighlighted(false)); // clear previous selection
                repaint();
            }

            Optional<Table> found = getIntersect(unproject(dragStart));
            if (!found.isPresent()) {
                setTarget(null);
                targetStart.set(origin);
                current = ActionType.Panning;
                return;
            }
            setTarget(found.get());
            targetStart.set(target.get());
            current = ActionType.Moving;
            repaint();
        }
    }

    public void setTarget(@Nullable Table table) {
        target.set(table);
        if (table != null) table.setHighlighted(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (current == ActionType.Moving) {
                if (target.get() == null) return;
                target.get().set(targetStart.add(unproject(e.getX(), e.getY())).minus(unproject(dragStart)));
                if (e.isControlDown()) target.get().div(gridSize).round().scale(gridSize);
                repaint();
            } else if (current == ActionType.Panning) {
                origin.set(targetStart.add(new Vector(e.getX(), e.getY()).decre(dragStart).div(scale)));
                repaint();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && current != ActionType.Creating) {
            Optional<Table> found = getIntersect(unproject(e.getX(), e.getY()));
            repaint();
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (connecting.get()) connecting.set(false);
            repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Vector pos = unproject(e.getX(), e.getY());
        if (adding_buf != null) {
            adding_buf.set(pos);
        }
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (current != ActionType.Creating) {
            scale *= Math.pow(0.95, e.getPreciseWheelRotation());
            repaint();
        }
    }

    public Optional<Table> getIntersect(Vector vector) {
        return find(table -> table.getShapeWorld().contains(vector)).findAny();
    }

    public Stream<Table> find(Predicate<Table> predicate) {
        return tables.stream().filter(predicate);
    }

    public Rectangle2D getAABB() {
        return tables.stream().map(e -> e.getShapeWorld().getBounds2D())
                .reduce(Rectangle2D::createUnion).orElse(new Rectangle2D.Double());
    }

    @Override
    public Line.LineStyle getLineStyle() {
        return lineStyle.get();
    }

    public Vector unproject(Vector vector) {
        return vector.divide(scale).decre(origin);
    }

    public Vector unproject(int x, int y) {
        return unproject(new Vector(x, y));
    }

    public Vector project(Vector vector) {
        return vector.add(origin).scale(scale);
    }

    public Vector project(double x, double y) {
        return project(new Vector(x, y));
    }

    @Override
    public Color foreground() {
        return darkMode.get() ? FOREGROUND_DARK : Color.BLACK;
    }

    @Override
    public Color background() {
        return darkMode.get() ? BACKGROUND_DARK : Color.WHITE;
    }

    @Override
    public Color fill() {
        return darkMode.get() ? FILL_DARK : Color.WHITE;
    }

    @Override
    public Color highlight() {
        return HIGHLIGHT;
    }

    enum ActionType {Moving, Panning, Creating}
}
