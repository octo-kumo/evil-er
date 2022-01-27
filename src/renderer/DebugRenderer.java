package renderer;

import model.*;
import model.entities.Attribute;
import model.entities.Entity;
import model.entities.Relationship;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DebugRenderer extends JComponent {
    public ArrayList<Drawable> drawables;

    public DebugRenderer() {
        setPreferredSize(new Dimension(1280, 720));
        drawables = new ArrayList<>();
        populate();
    }

    private void populate() {
        Entity a = new Entity().setName("Entity A").setPos(500, 500);
        Entity b = new Entity().setName("Entity B").setWeak(true).setPos(300, 500);
        a.addAttribute(new Attribute().setName("haha").setPos(0, 100));
        a.addAttribute(new Attribute().setName("nice").setWeak(true).setPos(200, 0));
        Relationship<Entity> r = new Relationship<>()
                .addNode(a, new Relationship.RelationshipSpec(false, false))
                .addNode(b, new Relationship.RelationshipSpec(true, true))
                .setName("R")
                .setPos(400, 300);
        drawables.add(a);
        drawables.add(b);
        drawables.add(r);
    }

    @Override
    protected void paintComponent(Graphics g1d) {
        super.paintComponent(g1d);
        Graphics2D g = (Graphics2D) g1d;
        drawables.forEach(d -> d.predraw(g));
        drawables.forEach(d -> d.draw(g));
    }

    public static void main(String... args) {
        JFrame frame = new JFrame("Debug");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new DebugRenderer());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
