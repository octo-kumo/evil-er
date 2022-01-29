package main.ui;

import main.EvilEr;
import model.entities.Entity;
import model.serializers.Serializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class EvilMenu extends JMenuBar {
    private EvilEr evilEr;

    public EvilMenu(EvilEr evilEr) {
        this.evilEr = evilEr;
        add(new JMenu("File") {{
            add(new JMenuItem("Open"));
            add(new JMenuItem(new AbstractAction("Save") {
                public void actionPerformed(ActionEvent ae) {
                    ArrayList<Entity> deserialized = Serializer.deserialize(Serializer.serialize(evilEr.diagramPanel.diagram.entities));
                    evilEr.diagramPanel.diagram.entities.clear();
                    evilEr.diagramPanel.diagram.entities.addAll(deserialized);
                    evilEr.diagramPanel.diagram.repaint();
                }
            }) {{
                setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }});
            add(new JMenuItem("Save as..."));
        }});
    }
}
