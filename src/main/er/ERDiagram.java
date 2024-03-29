package main.er;

import com.google.gson.stream.JsonReader;
import main.EvilEr;
import main.renderer.DiagramGraphics;
import model.Drawable;
import model.Vector;
import model.er.*;
import model.serializers.Serializer;
import model.ts.AddTransaction;
import model.ts.DeleteTransaction;
import model.ts.EntityTransaction;
import model.ts.TransactionGroup;
import org.jetbrains.annotations.Nullable;
import shapes.lines.Line;
import utils.Chooser;
import utils.callbacks.DrawContext;
import utils.models.Reactive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static main.renderer.DiagramGraphics.flatten;
import static utils.Prompts.report;

public class ERDiagram extends JComponent implements MouseListener, MouseMotionListener, DrawContext, Drawable, MouseWheelListener, FocusListener {

    public static final Color FILL_DARK = new Color(0x222222);
    public static final Color BACKGROUND_DARK = new Color(0x121212);
    public static final Color FOREGROUND_DARK = new Color(0xefefef);
    public static FontMetrics UNIVERSAL_METRICS;
    public static double GRID_SIZE = 16;
    public static Color HIGHLIGHT = new Color(0xff0000); // dummy color
    public final ERDiagramPanel diagramPanel;
    public final ERKeyManager keyManager;
    public final ArrayList<Entity> entities;
    public final ArrayList<Entity> selection = new ArrayList<>(); // selection
    public final Vector origin = new Vector();
    public final Vector panStart = new Vector();
    public final Reactive<Entity> target = new Reactive<>();
    public final Reactive<Boolean> debug = new Reactive<>(false);
    public final Reactive<Boolean> locked = new Reactive<>(false);
    public final Reactive<Boolean> aabb = new Reactive<>(false);
    public final Reactive<Boolean> grid = new Reactive<>(false);
    public final Reactive<Boolean> darkMode = new Reactive<>(false);
    public final Reactive<Vector> connectTarget = new Reactive<>(new Vector());
    public final Reactive<Entity.Type> addingType = new Reactive<>();
    public final Reactive<ActionType> action = new Reactive<>(ActionType.SELECTING);
    public final Stack<EntityTransaction> undoStack = new Stack<>();
    public final Stack<EntityTransaction> redoStack = new Stack<>();
    private final ArrayList<Entity> clipboard = new ArrayList<>(); // will be added to board on click, including new entity
    private final Vector mouseStart = new Vector(), mouseWorld = new Vector();
    public boolean acceptingKeys = true;
    public Reactive<Line.LineStyle> lineStyle = new Reactive<>(Line.LineStyle.STRAIGHT);
    public double scale = 1;
    public double exportScale = 4;
    /**
     * PARAMS
     */
    private boolean exporting = false;
    private Entity clipboardTarget;

    private File currentFile;
    private long lastRender = 0;

    public ERDiagram(ERDiagramPanel diagramPanel) {
        this.diagramPanel = diagramPanel;
        setEnabled(true);
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addFocusListener(this);
        setComponentPopupMenu(new ERPopupMenu(this));
        keyManager = new ERKeyManager(this);
        entities = new ArrayList<>();
        addListeners();
    }

    private void addListeners() {
        lineStyle.addListener(s -> repaint());
        locked.addListener(s -> {
            if (!s) setAddingType(null);
        });
    }


    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);
        UNIVERSAL_METRICS = getGraphics().getFontMetrics(getFont());
        DiagramGraphics g = new DiagramGraphics((Graphics2D) g1d, this);
        g.setColor(background());
        g.fillRect(0, 0, getWidth(), getHeight());
        AffineTransform transform = g.getTransform();
        g.scale(scale, scale);
        g.translate(origin.getX(), origin.getY());

        if (grid.get() || keyManager.CTRL) drawGrid(g);
        draw(g);
        g.setTransform(transform);
        g.drawStringCenter("@yun", getWidth() - 25, getHeight() - 15);
        if (currentFile != null)
            g.drawString(currentFile.getAbsolutePath() + (Node.HAS_NODE_CHANGED ? "*" : ""), 5, 10);
        if (hasFocus()) g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        if (debug.get()) {
            long et = -(lastRender - (lastRender = System.nanoTime()));
            double fps = 1000000000d / et;

            g.drawString(String.format("FPS %.3f", fps), 25, getHeight() - 45);
            g.drawString(String.format("\u00D7%.3f %s", scale, origin), 25, getHeight() - 30);
            g.drawString(String.format("%s %s (%d)", action.get(), target.get(), selection.size()), 25, getHeight() - 15);
        }
    }

    public void draw(DiagramGraphics g) {
        g.setRenderingHints(EvilEr.RENDER_HINTS);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(g.context.foreground());

        entities.forEach(d -> d.predraw(g));
        entities.forEach(d -> d.draw(g));

        if (exporting) return;

        g.setColor(g.context.highlight());
        selection.forEach(d -> g.draw(d.getShapeWorld()));
        if (target.nonNull()) g.dashed(target.get().getShapeWorld().getBounds2D());

        if (action.equal(ActionType.ADDING)) drawClipboard(g);
        if (action.equal(ActionType.CONNECTING)) drawPendingConnection(g);
        if (aabb.get()) g.draw(getAABB());
    }

    public BufferedImage export() {
        setTarget(null);
        selection.clear();

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

    public void delete() {
        selection.forEach(this::delete);
        selection.forEach(this::burnBridges);
        selection.clear();
        repaint();
    }

    public void delete(Entity entity) {
        if (target.get() == entity) setTarget(null);
        commit(new DeleteTransaction(entity));
    }

    public void copy(boolean cut) {
        clipboard.clear();
        HashMap<Entity, Entity> worldToClip = new HashMap<>();
        List<Entity> entityStream = selection.stream().filter(e -> !(e instanceof Attribute && selection.contains(((Attribute) e).getParent()))).map(entity -> {
            if (!cut) worldToClip.put(entity, entity = entity.clone());
            return entity;
        }).collect(Collectors.toList());
        System.out.println(worldToClip);
        entityStream.forEach(e -> {
            clipboard.add(e);
            e.decre(mouseWorld);
            if (cut) {
                ERDiagram.this.delete(e);
                ERDiagram.this.burnBridges(e, entityStream);
                return;
            }
            if (e instanceof Attribute) {
                double x = e.getX(), y = e.getY();
                ((Attribute) e).setParent(null);
                e.set(x, y);
            }
            if (e instanceof Relationship) {
                System.out.printf("\tHandling Relationship: %s%n", e);
                Relationship r = (Relationship) e;
                for (int i = 0; i < r.nodes.size(); i++) {
                    Entity key = r.nodes.get(i);
                    if (worldToClip.containsKey(key)) r.set(i, worldToClip.get(key));
                }
            }
        });
        action.set(ActionType.ADDING);
        repaint();
    }

    public void copy() {
        System.out.println(":: copy");
        copy(false);
    }

    public void cut() {
        System.out.println(":: cut");
        copy(true);
    }

    public void paste() {
        System.out.println(":: paste");
        selection.clear();
        List<Entity> list = new ArrayList<>();
        HashMap<Entity, Entity> clipToWorld = new HashMap<>();
        for (Entity entity : clipboard) {
            clipToWorld.put(entity, entity = entity.clone());

            entity.incre(mouseWorld);
            list.add(entity);
            selection.add(entity);
        }
        for (Entity entity : list) {
            if (entity instanceof Attribute && ((Attribute) entity).getParent() == null && clipboardTarget != null) {
                ((Attribute) entity).setParent(clipboardTarget);
                entity.decre(clipboardTarget);
            }
            if (entity instanceof Specialization && ((Specialization) entity).getSuperclass() == null)
                ((Specialization) entity).setSuperclass(clipboardTarget);
            if (entity instanceof Relationship) {
                Relationship r = (Relationship) entity;
                for (int i = 0; i < r.nodes.size(); i++) {
                    Entity key = r.nodes.get(i);
                    if (clipToWorld.containsKey(key)) r.nodes.set(i, clipToWorld.get(key));
                }
            }
        }
        commit(new TransactionGroup(list.stream().map(AddTransaction::new).toArray(AddTransaction[]::new)));
        if (!locked.get()) {
            setAddingType(null);
            action.set(ActionType.SELECTING);
        }
    }

    public void commit(EntityTransaction transaction) {
        Node.HAS_NODE_CHANGED = true;
        transaction.redo(entities);
        undoStack.push(transaction);
        redoStack.clear();
        repaint();
    }

    public void undo() {
        System.out.println(":: undo");
        if (undoStack.empty()) return;
        EntityTransaction lastAction = undoStack.pop();
        lastAction.undo(entities);
        redoStack.push(lastAction);
        repaint();
    }

    public void redo() {
        System.out.println(":: redo");
        if (redoStack.empty()) return;
        EntityTransaction lastUndidAction = redoStack.pop();
        lastUndidAction.redo(entities);
        undoStack.push(lastUndidAction);
        repaint();
    }

    public void burnBridges(Entity entity) {
        entities.parallelStream().filter(e -> e instanceof Relationship).forEach(r -> ((Relationship) r).remove(entity));
    }

    public void burnBridges(Entity entity, List<Entity> toIgnore) {
        entities.parallelStream().filter(e -> e instanceof Relationship).filter(e -> !toIgnore.contains(e)).forEach(r -> ((Relationship) r).remove(entity));
    }

    public void setAddingType(Entity.Type type) {
        if (action.equal(ActionType.SELECTING) && selection.isEmpty()) action.set(ActionType.ADDING);
        if (!action.equal(ActionType.ADDING)) return;

        addingType.set(type);
        clipboard.clear();

        if (type == null) {
            action.set(ActionType.SELECTING);
            return;
        }
        Entity entity = null;
        if (type == Entity.Type.Entity) clipboard.add(entity = new Entity().setName("Unnamed"));
        else if (type == Entity.Type.Relationship) clipboard.add(entity = new Relationship().setName("Unnamed"));
        else if (type == Entity.Type.Attribute) clipboard.add(entity = new Attribute().setName("Unnamed"));
        else if (type == Entity.Type.Specialization) clipboard.add(entity = new Specialization());
        if (entity != null) entity.set(0, 0);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow();
        mouseStart.set(e.getX(), e.getY());
        mouseWorld.set(unproject(mouseStart));
        if (SwingUtilities.isLeftMouseButton(e)) {
            switch (action.get()) {
                case ADDING:
                    paste();
                    repaint();
                    break;
                case CONNECTING:
                    if (!finishPendingConnection() && !tryStartConnectionAtMouse()) {
                        setTarget(null);
                        action.set(ActionType.SELECTING);
                        selection.clear();
                    }
                    repaint();
                    break;
                case SELECTING:
                    if (!e.isShiftDown()) selection.clear();
                    setTarget(null);
                    getIntersect(mouseWorld).ifPresent(entity -> {
                        if (selection.contains(entity)) {
                            selection.remove(entity);
                            setTarget(null);
                        } else setTarget(entity);
                    });
                    repaint();
                    break;
            }
            if (selection.isEmpty() && !target.nonNull()) {
                panStart.set(origin);
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            if (action.equal(ActionType.SELECTING)) getIntersect(mouseWorld).ifPresent(this::setTarget);
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            action.set(ActionType.SELECTING);
            selection.clear();
            clipboard.clear();
            tryStartConnectionAtMouse();
            repaint();
        }
    }

    public void setTarget(@Nullable Entity entity) {
        target.set(entity);

        if (entity != null) selection.add(entity);
        else {
            action.set(ActionType.SELECTING);
            diagramPanel.requestNameEdit(null);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) finishPendingConnection();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Vector mouse = new Vector(e.getX(), e.getY());
        Vector newMouse = unproject(mouse);
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (action.equal(ActionType.SELECTING)) {
                if (selection.isEmpty()) {
                    origin.set(panStart.add(mouse.minus(mouseStart).div(scale)));
                } else {
                    Vector diff = !e.isControlDown() ? newMouse.minus(mouseWorld) : newMouse.snapTo(GRID_SIZE).decre(mouseWorld.snapTo(GRID_SIZE)).incre(target.nonNull() ? target.get().snapTo(GRID_SIZE).minus(target.get()) : Vector.ZERO);
                    selection.stream().filter(entity -> !(entity instanceof Attribute) || !selection.contains(((Attribute) entity).getParent())).forEach(entity -> entity.incre(diff));
                }
                repaint();
            }
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            if (action.equal(ActionType.CONNECTING)) repaint();
        }
        mouseWorld.set(newMouse);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && !action.equal(ActionType.ADDING)) {
            Optional<Entity> found = getIntersect(unproject(e.getX(), e.getY()));
            if (e.getClickCount() == 2) {
                if (found.isPresent()) diagramPanel.requestNameEdit(found.get());
                else if (action.equal(ActionType.CONNECTING)) action.set(ActionType.SELECTING);
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
        mouseWorld.set(unproject(e.getX(), e.getY()));
        if (action.equal(ActionType.CONNECTING) && target.get() != null) {
            Optional<Entity> found = getIntersect(mouseWorld).filter(entity -> !(entity instanceof Relationship) && !(entity instanceof Attribute));
            connectTarget.set(found.isPresent() ? found.get() : mouseWorld);
        } else if (action.equal(ActionType.ADDING) && clipboard.size() > 0) {
            getIntersect(mouseWorld).ifPresent(entity -> clipboardTarget = entity);
        } else if (action.equal(ActionType.SELECTING)) {
            if (getIntersect(mouseWorld).isPresent()) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!action.equal(ActionType.ADDING)) {
            Vector mouse = new Vector(e.getX(), e.getY());
            Vector a = mouse.divide(scale);
            scale *= Math.pow(0.95, e.getPreciseWheelRotation());
            Vector b = mouse.divide(scale);
            origin.incre(b.minus(a));
            repaint();
        }
    }

    private AddTransaction processEntitiesBeforeAddition(Entity clipboardItem) {
        Vector pos = clipboardItem.add(mouseWorld); // translate to mouse position, from clipboard world
        Entity parent = null;
        if (clipboardItem instanceof Attribute) {
            if (((Attribute) clipboardItem).getParent() == null && clipboardTarget != null) parent = clipboardTarget;
        } else {
            if (clipboardItem instanceof Specialization && ((Specialization) clipboardItem).getSuperclass() == null && clipboardTarget != null)
                parent = clipboardTarget;
        }
        clipboardItem.set(pos);
        return new AddTransaction(clipboardItem, parent);
    }

    public Optional<Entity> getIntersect(Vector vector) {
        return find(entity -> entity.getShapeWorld().contains(vector)).findAny();
    }

    public Stream<Entity> find(Predicate<Entity> predicate) {
        return flatten(entities).filter(predicate);
    }

    private boolean tryStartConnectionAtMouse() {
        Optional<Entity> intersect = getIntersect(mouseWorld);
        if (intersect.isPresent() && intersect.get() instanceof Relationship) {
            action.set(ActionType.CONNECTING);
            selection.clear();
            setTarget(intersect.get());
            selection.addAll(((Relationship) intersect.get()).nodes);
            return true;
        }
        return false;
    }

    private boolean finishPendingConnection() {
        if (target.get() == null || connectTarget.get() == null ||
                !(target.get() instanceof Relationship) ||
                connectTarget.get().getClass() != Entity.class) return false;
        System.out.println(target.get() + " :: " + connectTarget.get());
        ((Relationship) target.get()).addNode((Entity) connectTarget.get(), new Relationship.RelationshipSpec("", false));
        selection.add((Entity) connectTarget.get());
        target.set(target.get());
        return true;
    }

    private void drawPendingConnection(DiagramGraphics g) {
        if (target.get() == null || connectTarget.get() == null) return;
        g.dashed(new Line2D.Double(target.get(), connectTarget.get()));
    }

    private void drawClipboard(DiagramGraphics g) {
        AffineTransform transform = g.getTransform();
        g.translate(mouseWorld);
        clipboard.forEach(clipboardItem -> {
            if (clipboardItem instanceof Specialization && ((Specialization) clipboardItem).getSuperclass() == null && clipboardTarget != null || clipboardItem instanceof Attribute && ((Attribute) clipboardItem).getParent() == null && clipboardTarget != null) {
                g.draw(new Line2D.Double(clipboardItem, clipboardTarget.minus(mouseWorld)));
            } else clipboardItem.predraw(g);
        });
        clipboard.forEach(clipboardItem -> clipboardItem.draw(g));
        g.setTransform(transform);
    }

    private void drawGrid(DiagramGraphics g) {
        Vector lt = origin.negate();
        g.setColor(g.context.highlight());
        Vector sz = new Vector(getWidth(), getHeight()).div(scale);
        double maxX = lt.getX() + sz.getX();
        double maxY = lt.getY() + sz.getY();
        double size = GRID_SIZE * 8;
        for (int x = (int) (Math.ceil(lt.getX() / size) * size); x < maxX; x += size)
            g.drawLine(x, (int) lt.getY(), x, (int) maxY);
        for (int y = (int) (Math.ceil(lt.getY() / size) * size); y < maxY; y += size)
            g.drawLine((int) lt.getX(), y, (int) maxX, y);
    }

    public Rectangle2D getAABB() {
        return flatten(entities).map(e -> e.getShapeWorld().getBounds2D()).reduce(Rectangle2D::createUnion).orElse(new Rectangle2D.Double());
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

    @Override
    public void focusGained(FocusEvent e) {
        repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        repaint();
    }

    public void readFromFile(File file) throws FileNotFoundException {
        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            ArrayList<Entity> deserialized = Serializer.deserialize(reader);
            reset();
            entities.addAll(deserialized);
            centralize();
            repaint();
        } catch (FileNotFoundException e) {
            reset();
            throw e;
        } catch (IOException e) {
            report(e);
            e.printStackTrace();
        }
    }

    private void reset() {
        selection.clear();
        entities.clear();
        scale = 1;
        origin.set(0, 0);
        undoStack.clear();
        redoStack.clear();
        addingType.set(Entity.Type.Entity);
        target.set((Entity) null);
        connectTarget.set((Vector) null);
        action.set(ActionType.SELECTING);
    }

    public void saveToFile(String parent) {
        if (currentFile != null) saveToFile(getCurrentFile());
        else saveAsFile(parent);
    }

    public void saveAsFile(String parent) {
        Chooser.jsonChooser.setCurrentDirectory(new File(parent));
        Chooser.jsonChooser.setSelectedFile(getCurrentFile());
        if (JFileChooser.APPROVE_OPTION == Chooser.jsonChooser.showSaveDialog(this))
            saveToFile(Chooser.jsonChooser.getFinal());
    }

    public void saveToFile(File file) {
        String json = Serializer.serialize(entities);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(json);
        } catch (IOException e) {
            report(e);
            e.printStackTrace();
        }
        setCurrentFile(file);
        Node.HAS_NODE_CHANGED = false;
        repaint();
    }

    public boolean drawDebugInfo() {
        return debug.get();
    }

    public boolean exporting() {
        return exporting;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFileQuietly(File currentFile) {
        this.currentFile = currentFile;
        repaint();
    }

    public void setCurrentFile(File currentFile) {
        if (Objects.equals(currentFile, getCurrentFile())) return;
        if (Node.HAS_NODE_CHANGED && JOptionPane.showConfirmDialog(this, "You have unsaved changes, discard?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;
        if (currentFile == null) {
            reset();
            this.currentFile = null;
            Node.HAS_NODE_CHANGED = false;
        } else {
            try {
                readFromFile(currentFile);
                this.currentFile = currentFile;
                Node.HAS_NODE_CHANGED = false;
            } catch (FileNotFoundException e) {
                this.currentFile = currentFile;
                Node.HAS_NODE_CHANGED = true;
                saveToFile(currentFile);
            } catch (Exception e) {
                report(e);
            }
        }
        repaint();
    }

    public void gridify() {
        gridify(entities);
    }

    public void gridify(ArrayList<? extends Entity> entities) {
        entities.forEach(entity -> {
            entity.snap(GRID_SIZE);
            gridify(entity.attributes);
        });
    }

    public void centralize() {
        Vector com = entities.parallelStream().map(e -> (Vector) e).reduce(Vector::add).orElse(Vector.ZERO).div(entities.size());
        origin.set(com.negate().add(getWidth() / 2d / scale, getHeight() / 2d / scale));
        repaint();
//        entities.add((Entity) new Entity("origin").set(origin));
    }

    public void fitScreen() {
        Rectangle2D aabb = getAABB();
        Rectangle bounds = getBounds();
        this.scale = Math.min(bounds.getWidth() / aabb.getWidth(), bounds.getHeight() / aabb.getHeight());
        this.origin.set(new Vector(aabb.getCenterX(), aabb.getCenterY())
                .negate()
                .add(getX() / scale, getY() / scale)
                .add(getWidth() / 2d / scale, getHeight() / 2d / scale));
        repaint();
    }

    enum ActionType {ADDING, CONNECTING, SELECTING}
}
