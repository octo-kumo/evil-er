package main.er;

import javax.swing.*;
import java.awt.*;

import static main.ui.Prompts.report;

public class EvilEr extends JPanel {
    public final ERControlPanel controlPanel;
    public final ERDiagramPanel diagramPanel;
    public final ERInfoPanel infoPanel;
    public JFrame frame;

    public EvilEr(JFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());

        diagramPanel = new ERDiagramPanel(this);
        add(diagramPanel, BorderLayout.CENTER);

        controlPanel = new ERControlPanel(this);
        add(controlPanel, BorderLayout.NORTH);

        infoPanel = new ERInfoPanel(this);
        add(infoPanel, BorderLayout.EAST);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(diagramPanel.diagram.keyManager);
    }

    public static void main(String... args) {
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            report(e);
            e.printStackTrace();
        }
        JFrame frame = new JFrame("Evil ER");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        EvilEr evilEr;
        frame.setContentPane(evilEr = new EvilEr(frame));
        frame.setJMenuBar(new ERMenu(evilEr));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
