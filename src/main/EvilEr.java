package main;

import main.ui.ControlPanel;
import main.ui.DiagramPanel;
import main.ui.EvilMenu;
import main.ui.InfoPanel;

import javax.swing.*;
import java.awt.*;

public class EvilEr extends JPanel {
    private static EvilEr evilEr;
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
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("Evil ER");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(evilEr = new EvilEr());
        frame.setJMenuBar(new EvilMenu(evilEr));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
