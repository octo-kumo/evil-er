package main.renderer;

import main.EvilEr;
import main.ui.DiagramPanel;
import main.ui.KeyManager;
import model.entities.Attribute;
import model.entities.DrawContext;
import model.entities.Entity;
import model.entities.Relationship;
import model.lines.Line;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static main.renderer.DiagramGraphics.flatten;

public class Diagram extends JComponent implements MouseListener, MouseMotionListener, DrawContext {
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

        Entity a = new Entity().setName("Entity A").setPos(500, 500);
        Entity b = new Entity().setName("Entity B").setWeak(true).setPos(300, 500);
        a.addAttribute(new Attribute().setName("haha").setPos(0, 100));
        a.addAttribute(new Attribute().setName("nice").setWeak(true).setPos(200, 0));
        Relationship<Entity> r = new Relationship<>()
                .addNode(a, new Relationship.RelationshipSpec(false, false))
                .addNode(b, new Relationship.RelationshipSpec(true, true))
                .setName("R")
                .setPos(400, 300);
        entities.add(a);
        entities.add(b);
        entities.add(r);
    }

    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);
        DiagramGraphics g = new DiagramGraphics((Graphics2D) g1d);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.setContext(this);

        entities.forEach(d -> d.predraw(g));
        entities.forEach(d -> d.draw(g));
        g.setColor(Color.GRAY);
        if (adding_buf != null) {
            if (adding_buf instanceof Attribute) {
                Entity parent = ((Attribute) adding_buf).parent;
                if (parent != null) g.draw(new Line2D.Double(parent.getX(), parent.getY(), adding_buf.x, adding_buf.y));
            }
            adding_buf.draw(g);
        }
    }

    public void delete() {
        List<Entity> targets = find(entity -> entity.highlighted).toList();
        targets.forEach(e -> {
            if (e instanceof Attribute) ((Attribute) e).parent.removeAttribute((Attribute) e);
            else entities.remove(e);
        });
        repaint();
    }

    /**
     * ADDING PARAMS
     */
    public Entity adding_buf;

    public void setAddingType(String type) {
        double x = 0, y = 0;
        if (adding_buf != null) {
            x = adding_buf.x;
            y = adding_buf.y;
        }
        if (type == null) {
            adding_buf = null;
            return;
        } else if (type.equals("entity")) adding_buf = new Entity().setName("Unnamed");
        else if (type.equals("relationship")) adding_buf = new Relationship<>().setName("Unnamed");
        else if (type.equals("attribute")) adding_buf = new Attribute().setName("Unnamed");
        else return;
        adding_buf.setPos(x, y);
    }

    private Entity target;
    private double sx, sy, ex, ey;

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            sx = e.getX();
            sy = e.getY();

            if (target != null && !e.isControlDown()) {
                find(entity -> entity.highlighted).forEach(entity -> entity.highlighted = false); // clear previous selection
                repaint();
            }

            Optional<Entity> found = getIntersect(e.getX(), e.getY());
            if (found.isEmpty()) {
                setTarget(null);
                diagramPanel.requestNameEdit(null);
                return;
            }
            setTarget(found.get());
            target.highlighted = true;
            ex = target.x;
            ey = target.y;
            repaint();
        }
    }

    public void setTarget(Entity entity) {
        listeners.forEach(e -> e.onChange(entity));
        target = entity;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (target == null) return;
        target.x = ex + (e.getX() - sx);
        target.y = ey + (e.getY() - sy);
        if (e.isControlDown()) {
            int gridSize = 20;
            target.x = ((int) ((target.x + gridSize / 2) / gridSize)) * gridSize;
            target.y = ((int) ((target.y + gridSize / 2) / gridSize)) * gridSize;
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (adding_buf != null) {
                if (adding_buf instanceof Attribute) {
                    Entity parent = ((Attribute) adding_buf).parent;
                    if (parent == null) return;
                    parent.addAttribute(adding_buf.setPos(adding_buf.x - parent.getX(), adding_buf.y - parent.getY()));
                } else entities.add(adding_buf);
                diagramPanel.requestNameEdit(adding_buf);
                adding_buf = null;
                evilEr.controlPanel.addType.clearSelection();
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
            adding_buf.x = e.getX();
            adding_buf.y = e.getY();
            if (adding_buf instanceof Attribute) {
                Optional<Entity> found = getIntersect(e.getX(), e.getY());
                found.ifPresent(entity -> ((Attribute) adding_buf).parent = entity);
            }
            repaint();
        }
    }

    public Optional<Entity> getIntersect(double x, double y) {
        return find(entity -> entity.getShapeWorld().contains(x, y)).findAny();
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
}
