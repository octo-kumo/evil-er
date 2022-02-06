package main.er;

import model.Vector;
import model.er.Entity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ERDiagramPanel extends JPanel implements ComponentListener {
    public final ERDiagram diagram;
    private final JTextField input;
    private Entity inputTarget = null;
    private EvilEr evilEr;

    public ERDiagramPanel(EvilEr evilEr) {
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
        add(diagram = new ERDiagram(this));
    }

    public void requestNameEdit(Entity entity) {
        if (inputTarget != null) inputTarget.setName(input.getText());

        if ((inputTarget = entity) == null) {
            input.setVisible(false);
            diagram.acceptingKeys = true;
        } else {
            input.setVisible(true);
            input.setText(entity.getName());
            input.setFont(new Font(null, Font.PLAIN, (int) (12 * diagram.scale)));
            entity.setName("");
            Vector pos = diagram.project(entity.minus(Entity.WIDTH / 2, Entity.HEIGHT / 4));
            input.setBounds((int) pos.getX(), (int) pos.getY(), (int) (Entity.WIDTH * diagram.scale), (int) (Entity.HEIGHT * diagram.scale / 2));
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
