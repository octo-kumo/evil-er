package main.renderer;

import main.ui.DiagramPanel;
import main.ui.components.KeyManager;
import model.Drawable;
import model.Vector;
import model.entities.Attribute;
import model.entities.Entity;
import model.entities.Relationship;
import model.entities.Specialization;
import model.i.DrawContext;
import model.lines.Line;
import model.others.Examples;
import model.others.Reactive;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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

public class Diagram extends JComponent implements MouseListener, MouseMotionListener, DrawContext, Drawable {

    public boolean acceptingKeys = true;
    public final KeyManager keyManager;
    public final ArrayList<Entity> entities;

    public Reactive<Line.LineStyle> lineStyle = new Reactive<>(Line.LineStyle.STRAIGHT);
    boolean exporting = false;

    /**
     * PARAMS
     */
    private Entity adding_buf;
    private final Vector dragStart = new Vector(), targetStart = new Vector();

    public final Reactive<Entity> target = new Reactive<>();
    public final Reactive<Entity.Type> addingType = new Reactive<>(Entity.Type.Select);
    public final Reactive<Boolean> connecting = new Reactive<>(false);
    public final Reactive<Boolean> locked = new Reactive<>(false);
    public final Reactive<Boolean> aabb = new Reactive<>(false);
    public final Reactive<Entity> connectTarget = new Reactive<>();

    private Relationship<Entity> connectBase;
    private final Vector connectPos = new Vector();
    private final DiagramPanel diagramPanel;

    public Diagram(DiagramPanel diagramPanel) {
        this.diagramPanel = diagramPanel;
        addMouseListener(this);
        addMouseMotionListener(this);
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
        draw(new DiagramGraphics((Graphics2D) g1d));
    }

    public void draw(DiagramGraphics g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.setContext(this);

        entities.forEach(d -> d.predraw(g));
        entities.forEach(d -> d.draw(g));

        if (exporting) return;
        g.drawStringCenter("@yun", getWidth() - 25, getHeight() - 15);
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
        BufferedImage img = new BufferedImage((int) aabb.getWidth(), (int) aabb.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, (int) aabb.getWidth(), (int) aabb.getHeight());
        g.translate(-aabb.getX(), -aabb.getY());
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
        entities.stream().filter(e -> e instanceof Relationship).map(e -> (Relationship<?>) e).forEach(r -> r.remove(entity));
    }

    public void setAddingType(Entity.Type type) {
        addingType.set(type);
        Vector pos = null;
        if (adding_buf != null) pos = adding_buf.clone();
        switch (type) {
            case Select:
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
        adding_buf.set(pos);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {

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

            Optional<Entity> found = getIntersect(dragStart);
            if (!found.isPresent()) {
                setTarget(null);
                diagramPanel.requestNameEdit(null);
                return;
            }
            setTarget(found.get());
            targetStart.set(target.get());
            repaint();
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
            if (target.get() == null) return;
            target.get().set(targetStart.add(e.getX(), e.getY()).minus(dragStart));
            if (e.isControlDown()) target.get().div(gridSize).round().scale(gridSize);
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (adding_buf != null) {
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
                Optional<Entity> found = getIntersect(e.getX(), e.getY());
                if (e.getClickCount() == 2 && found.isPresent()) diagramPanel.requestNameEdit(found.get());
            }
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
        if (adding_buf != null) {

            if (adding_buf instanceof Specialization)
                ((Specialization) adding_buf).setSuperclass(getIntersect(connectPos.set(e.getX(), e.getY()))
                        .orElse(null));
            if (!(adding_buf instanceof Specialization && ((Specialization) adding_buf).getSuperclass() != null))
                adding_buf.set(e.getX(), e.getY());
            if (adding_buf instanceof Attribute) getIntersect(connectPos.set(e.getX(), e.getY()))
                    .ifPresent(entity -> ((Attribute) adding_buf)
                            .setParent(entity).set(e.getX(), e.getY()));
        }
        if (connecting.get() && connectBase != null) {
            connectTarget.set(getIntersect(connectPos.set(e.getX(), e.getY()))
                    .filter(entity -> !(entity instanceof Relationship) && !(entity instanceof Attribute))
                    .orElse(null));
        }
        repaint();
    }

    public Optional<Entity> getIntersect(Vector vector) {
        return find(entity -> entity.getShapeWorld().contains(vector)).findAny();
    }

    public Optional<Entity> getIntersect(double x, double y) {
        return getIntersect(new Vector(x, y));
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
}
