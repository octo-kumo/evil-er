package main;

import main.ui.ControlPanel;
import main.ui.DiagramPanel;
import main.ui.InfoPanel;

import javax.swing.*;
import java.awt.*;

public class EvilEr extends JPanel {
    public final ControlPanel controlPanel;
    public final DiagramPanel diagramPanel;
    public final InfoPanel infoPanel;

    public EvilEr() {
        setLayout(new BorderLayout());

        diagramPanel = new DiagramPanel(this);
        add(diagramPanel, BorderLayout.CENTER);

        controlPanel = new ControlPanel(this);
        add(controlPanel, BorderLayout.NORTH);

        infoPanel = new InfoPanel(this);
        add(infoPanel, BorderLayout.EAST);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(diagramPanel.diagram.keyManager);
    }

    public static void main(String... args) {
        JFrame frame = new JFrame("Evil ER");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new EvilEr());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
