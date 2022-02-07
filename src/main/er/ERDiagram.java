package main.er;

import main.renderer.DiagramGraphics;
import main.ui.components.KeyManager;
import model.Drawable;
import model.Vector;
import model.er.Attribute;
import model.er.Entity;
import model.er.Relationship;
import model.er.Specialization;
import model.callbacks.DrawContext;
import shapes.lines.Line;
import utils.Examples;
import model.others.Reactive;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static main.renderer.DiagramGraphics.flatten;

public class ERDiagram extends JComponent implements MouseListener, MouseMotionListener, DrawContext, Drawable, MouseWheelListener {

    public boolean acceptingKeys = true;
    public final KeyManager keyManager;
    public final ArrayList<Entity> entities;

    public Reactive<Line.LineStyle> lineStyle = new Reactive<>(Line.LineStyle.STRAIGHT);
    boolean exporting = false;

    enum ActionType {Moving, Panning, Creating}

    /**
     * PARAMS
     */
    private Entity adding_buf;
    private ActionType current = ActionType.Panning;
    private final Vector dragStart = new Vector(), targetStart = new Vector();
    public final Vector origin = new Vector();
    public double scale = 1;
    public double exportScale = 4;

    public final Reactive<Entity> target = new Reactive<>();
    public final Reactive<Entity.Type> addingType = new Reactive<>(Entity.Type.Select);
    public final Reactive<Boolean> connecting = new Reactive<>(false);
    public final Reactive<Boolean> locked = new Reactive<>(false);
    public final Reactive<Boolean> aabb = new Reactive<>(false);
    public final Reactive<Boolean> grid = new Reactive<>(false);
    public final Reactive<Entity> connectTarget = new Reactive<>();

    private Relationship<Entity> connectBase;
    private final Vector connectPos = new Vector();
    private final ERDiagramPanel diagramPanel;

    public ERDiagram(ERDiagramPanel diagramPanel) {
        this.diagramPanel = diagramPanel;
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        keyManager = new KeyManager(this);

        entities = new ArrayList<>();
        Examples.populate(entities);
        addListeners();
    }

    private void addListeners() {
        lineStyle.addListener(s -> repaint());
        connecting.addListener(s -> connectBase = s ? connectBase : null);
        locked.addListener(s -> {
            if (!s) setAddingType(Entity.Type.Select);
        });
    }

    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);
        DiagramGraphics g = new DiagramGraphics((Graphics2D) g1d);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        AffineTransform transform = g.getTransform();
        g.scale(scale, scale);
        g.translate(origin.getX(), origin.getY());

        if (grid.get()) {
            Vector lt = origin.negate();
            g.setColor(Color.LIGHT_GRAY);
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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.setContext(this);

        entities.forEach(d -> d.predraw(g));
        entities.forEach(d -> d.draw(g));

        if (exporting) return;
        g.setColor(Color.GRAY);
        if (adding_buf != null) drawPendingAddition(g);
        if (connecting.get() && connectBase != null) drawPendingConnection(g);
        if (aabb.get()) g.draw(getAABB());
    }

    public BufferedImage export() {
        setTarget(null);
        find(Entity::isHighlighted).forEach(entity -> entity.setHighlighted(false));

        Rectangle2D aabb = getAABB();
        int padding = 20;
        aabb.add(aabb.getMinX() - padding, aabb.getMinY() - padding);
        aabb.add(aabb.getMaxX() + padding, aabb.getMaxY() + padding);
        aabb.setRect(aabb.getX() * exportScale, aabb.getY() * exportScale, aabb.getWidth() * exportScale, aabb.getHeight() * exportScale);
        BufferedImage img = new BufferedImage((int) aabb.getWidth(), (int) aabb.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, (int) aabb.getWidth(), (int) aabb.getHeight());
        g.translate(-aabb.getX(), -aabb.getY());
        g.scale(exportScale, exportScale);
        exporting = true;
        draw(new DiagramGraphics(g));
        exporting = false;
        g.dispose();
        return img;
    }

    public void delete() {
        List<Entity> targets = find(Entity::isHighlighted).collect(Collectors.toList());
        targets.forEach(this::delete);
        targets.forEach(this::burnBridges);
        repaint();
    }

    public void delete(Entity entity) {
        if (target.get() == entity) setTarget(null);
        if (entity instanceof Attribute) ((Attribute) entity).getParent().removeAttribute((Attribute) entity);
        else entities.remove(entity);
    }

    public void burnBridges(Entity entity) {
        entities.parallelStream().filter(e -> e instanceof Relationship).map(e -> (Relationship<?>) e).forEach(r -> r.remove(entity));
    }

    public void setAddingType(Entity.Type type) {
        addingType.set(type);
        Vector pos = null;
        if (adding_buf != null) pos = adding_buf.clone();
        switch (type) {
            case Select:
                current = ActionType.Panning;
                adding_buf = null;
                return;
            case Entity:
                adding_buf = new Entity().setName("Unnamed");
                break;
            case Relationship:
                adding_buf = new Relationship<>().setName("Unnamed");
                break;
            case Attribute:
                adding_buf = new Attribute().setParent(target.get()).setName("Unnamed");
                break;
            case Specialization:
                adding_buf = new Specialization(target.get());
                break;
        }
        current = ActionType.Creating;
        adding_buf.set(pos);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (current == ActionType.Creating && adding_buf != null) {
                if (adding_buf instanceof Attribute) {
                    Entity parent = ((Attribute) adding_buf).getParent();
                    if (parent == null) return;
                    parent.addAttribute((Attribute) adding_buf);
                } else entities.add(adding_buf);
                if (!(adding_buf instanceof Attribute)) {
                    adding_buf.setHighlighted(true);
                    setTarget(adding_buf);
                }
                if (connecting.get() && connectBase != null && adding_buf.getClass() == Entity.class)
                    connectBase.addNode(adding_buf, new Relationship.RelationshipSpec("", false));

                diagramPanel.requestNameEdit(adding_buf);
                setAddingType(!locked.get() ? Entity.Type.Select : addingType.get());
            } else {
                if (connecting.get() && connectTarget.get() != null && connectBase != null) {
                    connectBase.addNode(connectTarget.get(), new Relationship.RelationshipSpec("", false));
                    repaint();
                    return;
                }
                dragStart.set(e.getX(), e.getY());

                /* SELECT ELEMENT */
                /* IF NOT SELECT MULTIPLE, CLEAR HIGHLIGHT */
                if (target.get() != null && !e.isShiftDown()) {
                    find(Entity::isHighlighted).forEach(entity -> entity.setHighlighted(false)); // clear previous selection
                    repaint();
                }

                Optional<Entity> found = getIntersect(unproject(dragStart));
                if (!found.isPresent()) {
                    setTarget(null);
                    targetStart.set(origin);
                    current = ActionType.Panning;
                    diagramPanel.requestNameEdit(null);
                    return;
                }

                if (target.get() == found.get() || found.get().isHighlighted()) {
                    found.get().setHighlighted(false);
                    setTarget(null);
                    current = ActionType.Panning;
                    diagramPanel.requestNameEdit(null);
                    return;
                }

                setTarget(found.get());
                targetStart.set(target.get());
                current = ActionType.Moving;
                repaint();
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            dragStart.set(e.getX(), e.getY());
            setTarget(null);
            targetStart.set(origin);
            current = ActionType.Panning;
            diagramPanel.requestNameEdit(null);
        }
    }

    public void setTarget(@Nullable Entity entity) {
        target.set(entity);
        if (entity != null) entity.setHighlighted(true);
        if (connecting.get() && entity instanceof Relationship) connectBase = (Relationship<Entity>) entity;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    public static double gridSize = 20;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (current == ActionType.Moving && !connecting.get()) {
                if (target.get() == null) return;
                target.get().set(targetStart.add(unproject(e.getX(), e.getY())).minus(unproject(dragStart)));
                if (e.isControlDown()) target.get().div(gridSize).round().scale(gridSize);
                repaint();
            }
        }
        if (current == ActionType.Panning && (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isLeftMouseButton(e))) {
            origin.set(targetStart.add(new Vector(e.getX(), e.getY()).decre(dragStart).div(scale)));
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && current != ActionType.Creating) {
            Optional<Entity> found = getIntersect(unproject(e.getX(), e.getY()));
            if (e.getClickCount() == 2 && found.isPresent()) diagramPanel.requestNameEdit(found.get());
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
        if (current == ActionType.Creating) {
            if (adding_buf != null) {
                if (adding_buf instanceof Specialization)
                    ((Specialization) adding_buf).setSuperclass(getIntersect(connectPos.set(pos))
                            .orElse(null));
                if (!(adding_buf instanceof Specialization && ((Specialization) adding_buf).getSuperclass() != null))
                    adding_buf.set(pos);
                if (adding_buf instanceof Attribute) getIntersect(connectPos.set(pos))
                        .ifPresent(entity -> ((Attribute) adding_buf)
                                .setParent(entity).set(pos));
                repaint();
            }
        }
        if (connecting.get() && connectBase != null) {
            connectTarget.set(getIntersect(connectPos.set(pos))
                    .filter(entity -> !(entity instanceof Relationship) && !(entity instanceof Attribute))
                    .orElse(null));
            repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (current != ActionType.Creating) {
            scale *= Math.pow(0.95, e.getPreciseWheelRotation());
            repaint();
        }
    }

    public Optional<Entity> getIntersect(Vector vector) {
        return find(entity -> entity.getShapeWorld().contains(vector)).findAny();
    }

    public Stream<Entity> find(Predicate<Entity> predicate) {
        return flatten(entities).filter(predicate);
    }

    private void drawPendingConnection(DiagramGraphics g) {
        if (connectTarget.get() != null)
            g.dashed(new Line2D.Double(connectBase, connectTarget.get()));
        else
            g.dashed(new Line2D.Double(connectBase, connectPos));
    }

    private void drawPendingAddition(DiagramGraphics g) {
        AffineTransform transform = g.getTransform();
        if (adding_buf instanceof Attribute) {
            Entity parent = ((Attribute) adding_buf).getParent();
            if (parent != null) {
                g.draw(new Line2D.Double(parent, adding_buf));
                g.translate(parent.getX(), parent.getY());
            }
        }
        adding_buf.draw(g);
        g.setTransform(transform);
    }

    public Rectangle2D getAABB() {
        return flatten(entities).map(e -> e.getShapeWorld().getBounds2D())
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
}
