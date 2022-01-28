package main.ui;

import main.EvilEr;
import model.Vector;
import model.entities.Entity;
import model.lines.Line;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {

    public final NoneSelectedButtonGroup addType;

    public ControlPanel(EvilEr evilEr) {
        setLayout(new FlowLayout());

        addType = new NoneSelectedButtonGroup(e -> evilEr.diagramPanel.diagram.setAddingType(e.getActionCommand()));

        add(new JToggleButton("Entity") {{
            addType.add(this);
            setActionCommand("entity");
        }});
        add(new JToggleButton("Relationship") {{
            addType.add(this);
            setActionCommand("relationship");
        }});
        add(new JToggleButton("Attribute") {{
            addType.add(this);
            setActionCommand("attribute");
        }});
        add(new JComboBox<>(Line.LineStyle.values()) {{
            addActionListener(evt -> evilEr.diagramPanel.diagram.setStyle((Line.LineStyle) getSelectedItem()));
        }});
        add(new JButton("Regress Positions") {{
            addActionListener(evt -> {
                double total;
                int ops = 0;
                do {
                    total = Entity.applyForces(evilEr.diagramPanel.diagram.entities,
                            new Vector(evilEr.diagramPanel.diagram.getWidth() / 2d, evilEr.diagramPanel.diagram.getHeight() / 2d),
                            100);
                    ops++;
                } while (total > 0.1);
                System.out.println("Regression Complete, Operations = " + ops);
                evilEr.diagramPanel.diagram.repaint();
            });
        }});
    }
}
