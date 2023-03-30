package main;

import main.er.ERMenu;
import main.er.ERPopupMenu;
import model.er.Attribute;
import model.er.Entity;
import utils.models.Tuple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class GlassPane extends JComponent implements MouseListener {
    private static final String HAS_SHOWN_ONBOARDING = "hasShownOnboarding";
    private int process = 0;
    private final List<Tuple<Supplier<JComponent>, String, Runnable>> list;

    public GlassPane(JFrame frame, EvilEr evilEr) {
        setOpaque(false);
        if (!ERMenu.SETTINGS.getBoolean(HAS_SHOWN_ONBOARDING, false)) {
            frame.getGlassPane().setVisible(true);
            addMouseListener(this);
        }
        list = Arrays.asList(
                new Tuple<>(() -> evilEr.fileList, "You can open diagrams from here", () -> {
                }),
                new Tuple<>(() -> evilEr.splitPane, "This your main editor window", () -> {
                    ERPopupMenu popupMenu = (ERPopupMenu) evilEr.diagramPanel.diagram.getComponentPopupMenu();
                    popupMenu.show(evilEr.diagramPanel.diagram, evilEr.diagramPanel.diagram.getWidth() / 2, evilEr.diagramPanel.diagram.getHeight() / 2);
                    popupMenu.add.setSelected(true);
                    popupMenu.add.getPopupMenu().show(popupMenu.add, popupMenu.add.getWidth(), 0);
                }),
                new Tuple<>(() -> evilEr.diagramPanel, "Here is where you draw your diagram!", () -> {
                    evilEr.diagramPanel.diagram.setAddingType(Entity.Type.Entity);
                    evilEr.diagramPanel.diagram.paste();
                    evilEr.diagramPanel.diagram.centralize();
                    ERPopupMenu popupMenu = (ERPopupMenu) evilEr.diagramPanel.diagram.getComponentPopupMenu();
                    popupMenu.setVisible(false);
                    popupMenu.add.getPopupMenu().setVisible(false);
                }),
                new Tuple<>(() -> evilEr.diagramPanel, "Scroll to zoom in/out", () -> {
                    evilEr.diagramPanel.diagram.scale *= 2;
                    evilEr.diagramPanel.diagram.centralize();
                }),

                new Tuple<>(() -> evilEr.diagramPanel, "Left click to select/pan", () -> evilEr.diagramPanel.diagram.setTarget(evilEr.diagramPanel.diagram.entities.get(0))),
                new Tuple<>(() -> evilEr.infoPanel, "Here you can edit the entity!", () -> {
                    evilEr.diagramPanel.diagram.entities.get(0).setName(System.getProperty("user.name"));
                    evilEr.diagramPanel.diagram.setTarget(evilEr.diagramPanel.diagram.target.get());
                }),
                new Tuple<>(() -> evilEr.infoPanel.attributesSec, "Like adding attributes", () -> {
                    evilEr.diagramPanel.diagram.entities.get(0).addAttribute((Attribute) new Attribute()
                            .setDerived(false)
                            .setKey(true).setName("id")
                            .set(0, 100));
                    evilEr.diagramPanel.diagram.setTarget(evilEr.diagramPanel.diagram.target.get());
                }),
//                new Tuple<>(evilEr.infoPanel.)
                new Tuple<>(() -> evilEr.controlPanel, "This is your control panel! Its kinda useless >_<", () -> {
                }),
                new Tuple<>(() -> evilEr.frame.getJMenuBar(), "Be sure to check the menu!", () -> {
                }),
                new Tuple<>(() -> evilEr, "Have fun!", () -> {
                })
        );
    }

    @Override
    public void paintComponent(Graphics g1d) {
        Graphics2D g = (Graphics2D) g1d;
        g.setColor(new Color(0, 0, 0, 0.5f));
        if (process < list.size()) highlight(g, list.get(process).a.get(), list.get(process).b);
    }

    private void highlight(Graphics2D g, JComponent target, String text) {
        Area area = new Area();
        area.add(new Area(getBounds()));
        Point point = SwingUtilities.convertPoint(target, 0, 0, this);
        area.subtract(new Area(new Rectangle(point, target.getSize())));
        g.fill(area);
        Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
        System.out.println(bounds);
        g.setColor(Color.WHITE);
        g.fillRect(point.x, point.y, (int) bounds.getWidth(), (int) bounds.getHeight());
        g.setColor(Color.BLACK);
        g.drawString(text, point.x, (int) (point.y - bounds.getY()));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (process < list.size()) list.get(process).c.run();
        process += 1;
        if (process >= list.size()) {
            ERMenu.SETTINGS.putBoolean(HAS_SHOWN_ONBOARDING, true);
            removeMouseListener(this);
            setVisible(false);
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
