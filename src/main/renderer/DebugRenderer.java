//package main.renderer;
//
//import com.google.common.collect.Iterables;
//import model.entities.Attribute;
//import model.entities.Entity;
//import model.entities.Relationship;
//import model.lines.Line;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.Optional;
//import java.util.stream.Stream;
//
//public class DebugRenderer extends JComponent implements KeyListener, MouseListener, MouseMotionListener {
//    public ArrayList<Entity> entities;
//    public static Line.LineStyle currentLineStyle = Line.LineStyle.CURVE;
//
//    private boolean snap;
//    private final Iterator<Line.LineStyle> styleIterator = Iterables.cycle(Line.LineStyle.values()).iterator();
//
//    public DebugRenderer() {
//        setFocusable(true);
//        setPreferredSize(new Dimension(1280, 720));
//        addKeyListener(this);
//        addMouseListener(this);
//        addMouseMotionListener(this);
//        entities = new ArrayList<>();
//        populate();
//    }
//
//    private void populate() {
//        Entity a = new Entity().setName("Entity A").setPos(500, 500);
//        Entity b = new Entity().setName("Entity B").setWeak(true).setPos(300, 500);
//        a.addAttribute(new Attribute().setName("haha").setPos(0, 100));
//        a.addAttribute(new Attribute().setName("nice").setWeak(true).setPos(200, 0));
//        Relationship<Entity> r = new Relationship<>()
//                .addNode(a, new Relationship.RelationshipSpec(false, false))
//                .addNode(b, new Relationship.RelationshipSpec(true, true))
//                .setName("R")
//                .setPos(400, 300);
//        entities.add(a);
//        entities.add(b);
//        entities.add(r);
//    }
//
//    @Override
//    protected void paintComponent(Graphics g1d) {
//        super.paintComponent(g1d);
//        Canvas g = (Canvas) g1d;
//        if (snap) g.drawString("SNAP", 50, 50);
//        entities.forEach(d -> d.predraw(g));
//        entities.forEach(d -> d.draw(g));
//    }
//
//    public static void main(String... args) {
//        JFrame frame = new JFrame("Debug");
//        frame.setAlwaysOnTop(true);
//        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.setContentPane(new DebugRenderer());
//        frame.pack();
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//    }
//
//    Entity target;
//    double sx, sy, ex, ey;
//
//    @Override
//    public void mousePressed(MouseEvent e) {
//        sx = e.getX();
//        sy = e.getY();
//        Optional<Entity> found = flatten(entities)
//                .filter(entity -> entity.getShapeWorld().contains(sx, sy)).findAny();
//        if (found.isEmpty()) return;
//        target = found.get();
//        ex = target.x;
//        ey = target.y;
//        repaint();
//    }
//
//    public Stream<Entity> flatten(ArrayList<? extends Entity> entities) {
//        return Stream.concat(entities.stream(), entities.stream().flatMap(e -> flatten(e.attributes)));
//    }
//
//    @Override
//    public void mouseReleased(MouseEvent e) {
//        target = null;
//    }
//
//    @Override
//    public void mouseDragged(MouseEvent e) {
//        if (target == null) return;
//        target.x = ex + (e.getX() - sx);
//        target.y = ey + (e.getY() - sy);
//        if (snap) {
//            int gridSize = 20;
//            target.x = ((int) ((target.x + gridSize / 2) / gridSize)) * gridSize;
//            target.y = ((int) ((target.y + gridSize / 2) / gridSize)) * gridSize;
//        }
//        repaint();
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        switch (e.getKeyCode()) {
//            case KeyEvent.VK_CONTROL -> this.snap = true;
//            case KeyEvent.VK_S -> currentLineStyle = styleIterator.next();
//        }
//        repaint();
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//        switch (e.getKeyCode()) {
//            case KeyEvent.VK_CONTROL -> this.snap = false;
//        }
//        repaint();
//    }
//
//    @Override
//    public void keyTyped(KeyEvent e) {
//
//    }
//
//    @Override
//    public void mouseClicked(MouseEvent e) {
//
//    }
//
//    @Override
//    public void mouseEntered(MouseEvent e) {
//
//    }
//
//    @Override
//    public void mouseExited(MouseEvent e) {
//
//    }
//
//    @Override
//    public void mouseMoved(MouseEvent e) {
//
//    }
//}
