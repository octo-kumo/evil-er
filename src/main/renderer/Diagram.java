package main.renderer;

import main.EvilEr;
import main.ui.DiagramPanel;
import main.ui.components.KeyManager;
import model.Drawable;
import model.Vector;
import model.entities.Attribute;
import model.entities.Entity;
import model.entities.Relationship;
import model.i.ChangeListener;
import model.i.DrawContext;
import model.lines.Line;
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
    private final EvilEr evilEr;
    private final DiagramPanel diagramPanel;

    public boolean acceptingKeys = true;
    public final KeyManager keyManager;
    public final ArrayList<Entity> entities;
    public final ArrayList<ChangeListener<Entity>> listeners;

    public Line.LineStyle style = Line.LineStyle.STRAIGHT;

    public Diagram(EvilEr evilEr, DiagramPanel diagramPanel) {
        this.evilEr = evilEr;
        this.diagramPanel = diagramPanel;
        addMouseListener(this);
        addMouseMotionListener(this);
        keyManager = new KeyManager(this);

        entities = new ArrayList<>();
        listeners = new ArrayList<>();

        Entity a = new Entity().setName("Entity A");
        a.set(500, 500);
        Entity b = new Entity().setName("Entity B").setWeak(true);
        b.set(300, 500);
        a.addAttribute((Attribute) new Attribute().setName("haha").set(0, 100));
        a.addAttribute((Attribute) new Attribute().setName("nice").setWeak(true).set(200, 0));
        Relationship<Entity> r = new Relationship<>();
        r.addNode(a, new Relationship.RelationshipSpec("1", false));
        r.addNode(b, new Relationship.RelationshipSpec("N", true));
        r.setName("R");
        r.set(400, 300);
        entities.add(a);
        entities.add(b);
        entities.add(r);
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
        g.setColor(Color.GRAY);
        if (adding_buf != null) drawPendingAddition(g);
        if (connecting && connectBase != null) drawPendingConnection(g);
//        g.draw(getAABB());
    }

    boolean exporting = false;

    public BufferedImage export() {
        setTarget(null);
        find(Entity::isHighlighted).forEach(entity -> entity.setHighlighted(false));

        Rectangle2D aabb = getAABB();
        int padding = 20;
        aabb.add(aabb.getMinX() - padding, aabb.getMinY() - padding);
        aabb.add(aabb.getMaxX() + padding, aabb.getMaxY() + padding);
        BufferedImage img = new BufferedImage((int) aabb.getWidth(), (int) aabb.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.translate(-aabb.getX(), -aabb.getY());
        exporting = true;
        evilEr.diagramPanel.diagram.draw(new DiagramGraphics(g));
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
        if (target == entity) setTarget(null);
        if (entity instanceof Attribute) ((Attribute) entity).getParent().removeAttribute((Attribute) entity);
        else entities.remove(entity);
    }

    public void burnBridges(Entity entity) {
        entities.stream().filter(e -> e instanceof Relationship).map(e -> (Relationship<?>) e).forEach(r -> r.remove(entity));
    }

    /**
     * PARAMS
     */
    private Entity adding_buf;
    private Entity target;
    private final Vector dragStart = new Vector(), targetStart = new Vector();

    private boolean connecting;
    private Relationship<Entity> connectBase;
    private Entity connectTarget;
    private Vector connectPos = new Vector();

    public void setAddingType(Entity.Types type) {
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
                adding_buf = new Attribute().setParent(target).setName("Unnamed");
                break;
        }
        adding_buf.set(pos);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {

            if (connecting && connectTarget != null && connectBase != null) {
                connectBase.addNode(connectTarget, new Relationship.RelationshipSpec("", false));
                repaint();
                return;
            }
            dragStart.set(e.getX(), e.getY());

            /* SELECT ELEMENT */
            /* IF NOT SELECT MULTIPLE, CLEAR HIGHLIGHT */
            if (target != null && !e.isShiftDown()) {
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
            targetStart.set(target);
            repaint();
        }
    }

    public void setTarget(@Nullable Entity entity) {
        listeners.forEach(e -> e.onChange(entity));
        target = entity;
        if (entity != null) target.setHighlighted(true);
        if (connecting && entity instanceof Relationship) connectBase = (Relationship<Entity>) entity;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    public static double gridSize = 20;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (target == null) return;
        target.set(targetStart.add(e.getX(), e.getY()).minus(dragStart));
        if (e.isControlDown()) target.set(target).div(gridSize).round().scale(gridSize);
        repaint();
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
                adding_buf.setHighlighted(true);
                setTarget(adding_buf);
                if (connecting && connectBase != null && adding_buf.getClass() == Entity.class) {
                    connectBase.addNode(adding_buf, new Relationship.RelationshipSpec("", false));
                }
                diagramPanel.requestNameEdit(adding_buf);
                if (!evilEr.controlPanel.lock.isSelected()) {
                    evilEr.controlPanel.typeBox.setSelectedItem(Entity.Types.Select);
                    adding_buf = null;
                } else setAddingType((Entity.Types) evilEr.controlPanel.typeBox.getSelectedItem());
            } else {
                Optional<Entity> found = getIntersect(e.getX(), e.getY());
                if (e.getClickCount() == 2 && found.isPresent()) diagramPanel.requestNameEdit(found.get());
            }
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
            adding_buf.set(e.getX(), e.getY());
            if (adding_buf instanceof Attribute) getIntersect(connectPos.set(e.getX(), e.getY()))
                    .ifPresent(entity -> ((Attribute) adding_buf)
                            .setParent(entity).set(e.getX(), e.getY()));
        }
        if (connecting && connectBase != null) {
            connectTarget = getIntersect(connectPos.set(e.getX(), e.getY()))
                    .filter(entity -> !(entity instanceof Relationship) && !(entity instanceof Attribute))
                    .orElse(null);
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

    public void setStyle(Line.LineStyle style) {
        this.style = style;
        repaint();
    }

    @Override
    public Line.LineStyle getStyle() {
        return style;
    }

    public void setConnecting(boolean connecting) {
        this.connecting = connecting;
    }

    private void drawPendingConnection(DiagramGraphics g) {
        if (connectTarget != null)
            g.dashed(new Line2D.Double(connectBase, connectTarget));
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
        return flatten(evilEr.diagramPanel.diagram.entities).map(e -> e.getShapeWorld().getBounds2D())
                .reduce(Rectangle2D::createUnion).orElse(new Rectangle2D.Double());
    }
}
