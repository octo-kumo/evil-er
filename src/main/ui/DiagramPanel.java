package main.ui;

import main.EvilEr;
import model.entities.Entity;
import main.renderer.Diagram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DiagramPanel extends JPanel implements ComponentListener {
    public final Diagram diagram;
    private final JTextField input;
    private Entity inputTarget = null;
    private EvilEr evilEr;

    public DiagramPanel(EvilEr evilEr) {
        this.evilEr = evilEr;
        setLayout(null);
        addComponentListener(this);
        setPreferredSize(new Dimension(1280, 720));

        add(input = new JTextField() {{
            setVisible(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setHorizontalAlignment(CENTER);
            addActionListener(e -> requestNameEdit(null));
        }});
        add(diagram = new Diagram(evilEr, this));
    }

    public void requestNameEdit(Entity entity) {
        if (inputTarget != null) inputTarget.name = input.getText();

        if ((inputTarget = entity) == null) {
            input.setVisible(false);
            diagram.acceptingKeys = true;
        } else {
            input.setVisible(true);
            input.setText(entity.name);
            entity.name = "";
            input.setBounds((int) (entity.getX() - Entity.WIDTH / 2), (int) (entity.getY() - Entity.HEIGHT / 4), (int) Entity.WIDTH, (int) Entity.HEIGHT / 2);
            input.grabFocus();
            input.selectAll();
            diagram.acceptingKeys = false;
        }
        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        diagram.setSize(getWidth(), getHeight());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        requestNameEdit(null);
    }
}
